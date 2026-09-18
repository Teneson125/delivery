package com.zuufa.delivery.provider.ekart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.zuufa.delivery.config.EkartDeliveryProperties;
import com.zuufa.delivery.dto.*;
import com.zuufa.delivery.entity.DeliveryProviderConfig;
import com.zuufa.delivery.provider.dto.*;
import com.zuufa.delivery.provider.ekart.dto.*;
import com.zuufa.delivery.repository.DeliveryProviderConfigRepository;
import com.zuufa.delivery.service.EkartAccountService;
import com.zuufa.delivery.service.impl.DeliveryProviderSettingsServiceImpl;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.web.client.RestClient;

/** Offline executable contract checks: plain java, no test framework or dependency download. */
public class EkartContractChecks {
    private static int checks;
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        var cipher = new EkartCredentialCipher(Base64.getEncoder().encodeToString(new byte[32]));
        String encrypted = cipher.encrypt(" exact password ");
        check(!encrypted.contains("password"), "encrypted credentials");
        check(cipher.decrypt(encrypted).equals(" exact password "), "password whitespace preserved");
        check(!encrypted.equals(cipher.encrypt(" exact password ")), "random nonce");
        rejects(() -> new EkartCredentialCipher("").encrypt("secret"), "missing key blocks saving");
        rejects(() -> cipher.decrypt(encrypted.substring(0, encrypted.length() - 5) + "AAAAA"), "tampered ciphertext rejected");

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var calls = new AtomicInteger();
        var addressAck = new AtomicReference<>(true);
        var shipmentAck = new AtomicReference<>(true);
        var registered = new AtomicReference<>(false);
        var lastShipment = new AtomicReference<String>();
        var lastWebhook = new AtomicReference<String>();
        var rate = new AtomicReference<>("42.50");
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String response;
            if (path.startsWith("/integrations/v2/auth/token/")) {
                calls.incrementAndGet();
                response = "{\"access_token\":\"mock-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}";
            } else if (path.equals("/api/v2/addresses")) {
                response = "[{\"alias\":\"pickup\",\"phone\":9876543210,\"address_line1\":\"Warehouse\",\"pincode\":560001,\"city\":\"Bengaluru\",\"state\":\"Karnataka\",\"country\":\"India\"}"
                        + (registered.get() ? ",{\"alias\":\"return\",\"pincode\":560002}" : "") + "]";
            } else if (path.equals("/api/v2/address")) {
                var data = mapper.readTree(body);
                check(data.get("phone").isIntegralNumber() && data.get("pincode").isIntegralNumber(), "numeric address fields");
                check(data.has("address_line1"), "documented address field names");
                if (addressAck.get()) registered.set(true);
                response = "{\"status\":" + addressAck.get() + ",\"alias\":\"return\",\"remark\":\"result\"}";
            } else if (path.equals("/api/v1/package/create")) {
                lastShipment.set(body);
                response = "{\"status\":" + shipmentAck.get() + ",\"tracking_id\":\"EK123\",\"vendor\":\"EKART\",\"remark\":\"result\"}";
            } else if (path.equals("/data/v3/serviceability")) {
                response = "[{\"tat\":{\"min\":2,\"max\":4},\"forwardDeliveredCharges\":{\"totalForwardDeliveredEstimate\":"
                        + (rate.get() == null ? "null" : "\"" + rate.get() + "\"") + "}}]";
            } else if (path.equals("/api/v2/webhook")) {
                if (!body.isBlank()) lastWebhook.set(body);
                response = "{\"id\":\"hook1\",\"url\":\"https://receiver.example/callback\",\"secret\":\"never-return-this\",\"topics\":[\"track_updated\"],\"active\":true}";
                if (exchange.getRequestMethod().equals("GET")) response = "[" + response + "]";
            } else if (path.startsWith("/api/v1/track/")) {
                response = "{\"track\":{\"status\":\"Delivered\"}}";
            } else { exchange.sendResponseHeaders(404, -1); exchange.close(); return; }
            if (!path.startsWith("/integrations/") && !path.startsWith("/api/v1/track/")) {
                check("Bearer mock-token".equals(exchange.getRequestHeaders().getFirst("Authorization")), "authenticated provider call");
            }
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        try {
            var properties = new EkartDeliveryProperties();
            properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
            properties.setLiveCallsEnabled(true);
            var auth = new EkartAuthClient(properties, RestClient.builder());
            var api = new EkartApiClient(properties, RestClient.builder());
            UUID tenant = UUID.randomUUID();
            var saved = new AtomicReference<DeliveryProviderConfig>();
            var repo = (DeliveryProviderConfigRepository) Proxy.newProxyInstance(EkartContractChecks.class.getClassLoader(),
                    new Class[]{DeliveryProviderConfigRepository.class}, (proxy, method, values) -> {
                        if (method.getName().equals("findByTenantIdAndProvider")) return tenant.equals(values[0]) ? Optional.ofNullable(saved.get()) : Optional.empty();
                        if (method.getName().equals("save")) { saved.set((DeliveryProviderConfig) values[0]); return values[0]; }
                        throw new UnsupportedOperationException(method.getName());
                    });
            var account = new EkartAccountService(repo, auth, api, cipher, mapper);
            var settingsService = new DeliveryProviderSettingsServiceImpl(repo, mapper, auth, cipher, account, properties);
            var config = settingsService.saveEkartConfig(tenant, new EkartProviderConfigRequest(false,
                    new EkartProviderCredentialsRequest("client-id", "username", " password ", null),
                    new EkartProviderSettingsRequest(null, null, null, "Prepaid", "SURFACE")));
            check(config.configured() && !config.enabled(), "credentials can be saved before addresses");
            check(!mapper.writeValueAsString(config).contains(" password "), "credentials masked");
            check(saved.get().getEncryptedCredentials().startsWith("v1:"), "encrypted database value");
            check(account.addresses(tenant).getFirst().pincode().equals("560001"), "upstream numeric pincode decoded");
            rejects(() -> account.addresses(UUID.randomUUID()), "tenant isolation");
            rejects(() -> settingsService.saveEkartConfig(tenant, new EkartProviderConfigRequest(true, null,
                    new EkartProviderSettingsRequest("999999", "fake", "fake", "Prepaid", "SURFACE"))), "unregistered alias rejected");
            var address = new EkartAddressRequest("return", "9876543210", "Returns", "", "560002", "Bengaluru", "Karnataka", "India");
            addressAck.set(false);
            rejects(() -> account.addAddress(tenant, address), "false address acknowledgement rejected");
            addressAck.set(true);
            check(account.addAddress(tenant, address).size() == 2, "create then provider reload");
            rejects(() -> account.addAddress(tenant, address), "duplicate alias blocked");
            config = settingsService.saveEkartConfig(tenant, new EkartProviderConfigRequest(true, null,
                    new EkartProviderSettingsRequest("999999", "pickup", "return", "Prepaid", "SURFACE")));
            check(config.settings().pickupPincode().equals("560001"), "pincode derived from Ekart");
            check(calls.get() == 1, "token cached for same credentials");
            settingsService.testEkartConnection(tenant);
            check(calls.get() == 2, "connection test refreshes token");
            check(!mapper.writeValueAsString(account.webhooks(tenant)).contains("never-return-this"), "webhook secret redacted");
            account.saveWebhook(tenant, null, new EkartWebhookRequest("https://receiver.example/callback", "new-secret", List.of("track_updated"), true));
            check(mapper.readTree(lastWebhook.get()).get("secret").asText().equals("new-secret"), "webhook contract");
            rejects(() -> account.saveWebhook(tenant, "unowned", new EkartWebhookRequest("https://receiver.example/callback", "new-secret", List.of("track_updated"), true)), "webhook ownership");

            var details = new EkartShipmentDetails("Seller", "Billing address", "", "INV-1", LocalDate.of(2026, 9, 18), "Books", "Two books",
                    new BigDecimal("118"), new BigDecimal("18"), new BigDecimal("100"), BigDecimal.ZERO, "Prepaid", BigDecimal.ZERO, 500, 20, 10, 5);
            var items = List.of(new DeliveryQuoteItemRequest(UUID.randomUUID(), UUID.randomUUID(), 2, 250, 20, 10, 2, new BigDecimal("50")));
            var destination = new DeliveryAddressRequest("Buyer", "+91 9876543210", "Customer street", null, "Chennai", "Tamil Nadu", "IN", "600001");
            var request = new CreateShipmentProviderRequest(tenant, UUID.randomUUID(), new BigDecimal("100"), items, destination, details);
            var context = new DeliveryProviderContext(tenant, saved.get().getEncryptedCredentials(), saved.get().getSettingsJson(), null, BigDecimal.ZERO, null, 0, 0);
            var provider = new EkartDeliveryProvider(auth, api, mapper, cipher);
            var shipment = provider.createShipment(request, context);
            check("EK123".equals(shipment.providerShipmentId()), "shipment acknowledged");
            var payload = mapper.readTree(lastShipment.get());
            check(payload.at("/pickup_location/name").asText().equals("pickup"), "pickup alias object");
            check(payload.at("/return_location/name").asText().equals("return"), "separate return alias object");
            check(payload.at("/drop_location/pin").asInt() == 600001, "complete customer location");
            check(!payload.has("pickup_address_id") && !payload.has("breadth") && payload.has("width"), "no obsolete shipment fields");
            check(payload.get("cod_amount").asInt() == 0 && payload.get("tax_value").asInt() == 18, "actual invoice and payment values");
            shipmentAck.set(false);
            check(provider.createShipment(request, context).providerShipmentId() == null, "false shipment acknowledgement rejected");
            rejects(() -> EkartShipmentPayload.build(new CreateShipmentProviderRequest(tenant, UUID.randomUUID(), BigDecimal.ONE, items, destination, null),
                    new EkartSettings("560001", "pickup", "return", "Prepaid", "SURFACE")), "missing invoice blocked");
            check(provider.track(new TrackShipmentProviderRequest(tenant, "EK123", "EK123"), context).status().equals("Delivered"), "nested tracking status");
            var quoteRequest = new DeliveryQuoteProviderRequest(tenant, new BigDecimal("118"), items, destination);
            check(provider.quote(quoteRequest, context).serviceable(), "V3 rate available");
            rate.set(null);
            check(!provider.quote(quoteRequest, context).serviceable(), "missing rate does not become free delivery");
            properties.setLiveCallsEnabled(false);
            rejects(() -> account.addresses(tenant), "runtime switch blocks provider calls");
            check(!settingsService.testEkartConnection(tenant).ok(), "runtime switch blocks connection test");
            var disabled = settingsService.saveEkartConfig(tenant, new EkartProviderConfigRequest(false, null, config.settings()));
            check(!disabled.enabled(), "disable works while provider offline");
        } finally { server.stop(0); }
        System.out.println("Ekart contract checks passed: " + checks);
    }

    private static void check(boolean ok, String name) {
        if (!ok) throw new AssertionError(name);
        checks++;
    }
    private static void rejects(Runnable call, String name) {
        try { call.run(); } catch (RuntimeException expected) { checks++; return; }
        throw new AssertionError(name);
    }
}
