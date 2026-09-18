package com.zuufa.delivery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuufa.delivery.dto.*;
import com.zuufa.delivery.entity.DeliveryProviderConfig;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.provider.dto.DeliveryProviderContext;
import com.zuufa.delivery.provider.ekart.*;
import com.zuufa.delivery.provider.ekart.dto.*;
import com.zuufa.delivery.repository.DeliveryProviderConfigRepository;
import com.zuufa.exception.BadRequestException;
import com.zuufa.exception.ServiceException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EkartAccountService {
    private final DeliveryProviderConfigRepository configs;
    private final EkartAuthClient auth;
    private final EkartApiClient api;
    private final EkartCredentialCipher cipher;
    private final ObjectMapper mapper;

    public String authorization(DeliveryProviderConfig config) {
        try {
            EkartCredentials credentials = mapper.readValue(cipher.decrypt(config.getEncryptedCredentials()), EkartCredentials.class);
            return auth.getAuthorizationHeader(new DeliveryProviderContext(config.getTenantId(), config.getEncryptedCredentials(),
                    config.getSettingsJson(), null, BigDecimal.ZERO, null, 0, 0), credentials);
        } catch (ServiceException e) { throw e; }
        catch (Exception e) { throw new BadRequestException("Unable to connect to Ekart. Check the saved credentials and try again."); }
    }

    public List<EkartAddress> addresses(UUID tenantId) { return addresses(config(tenantId)); }

    public List<EkartAddress> addresses(DeliveryProviderConfig config) {
        return providerCall(() -> {
            List<EkartAddress> result = api.addresses(authorization(config));
            if (result == null) throw new IllegalStateException();
            return result;
        });
    }

    public List<EkartAddress> addAddress(UUID tenantId, EkartAddressRequest request) {
        DeliveryProviderConfig config = config(tenantId);
        if (addresses(config).stream().anyMatch(a -> a.alias().equals(request.alias().trim()))) {
            throw new BadRequestException("This alias already exists in Ekart. Choose the existing address or use another alias.");
        }
        providerCall(() -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("alias", request.alias().trim()); body.put("phone", Long.valueOf(request.phone()));
            body.put("address_line1", request.addressLine1().trim()); body.put("address_line2", request.addressLine2());
            body.put("pincode", Integer.valueOf(request.pincode())); body.put("city", request.city().trim());
            body.put("state", request.state().trim()); body.put("country", request.country());
            Map<String, Object> acknowledgement = api.addAddress(authorization(config), body);
            if (acknowledgement == null || !Boolean.TRUE.equals(acknowledgement.get("status"))) {
                throw new BadRequestException("Ekart did not accept the address. Check its details and refresh before retrying.");
            }
            return true;
        });
        // The provider list remains the authority, including when registration is eventually consistent.
        return addresses(config);
    }

    public EkartAddress requireAddress(List<EkartAddress> addresses, String alias) {
        if (alias == null || alias.isBlank()) throw new BadRequestException("Select an address registered with Ekart.");
        return addresses.stream().filter(a -> alias.equals(a.alias())).findFirst()
                .orElseThrow(() -> new BadRequestException("Selected address is no longer registered with this Ekart account. Refresh the address list."));
    }

    public List<EkartWebhookResponse> webhooks(UUID tenantId) {
        return providerCall(() -> {
            List<Map<String, Object>> result = api.webhooks(authorization(config(tenantId)));
            if (result == null) throw new IllegalStateException();
            return result.stream().map(this::redactWebhook).toList();
        });
    }

    public EkartWebhookResponse saveWebhook(UUID tenantId, String id, EkartWebhookRequest request) {
        URI uri;
        try { uri = URI.create(request.url()); }
        catch (IllegalArgumentException e) { throw new BadRequestException("Enter a valid HTTPS webhook URL."); }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null) {
            throw new BadRequestException("Enter an HTTPS webhook URL without credentials or a fragment.");
        }
        if (id != null && webhooks(tenantId).stream().noneMatch(w -> id.equals(w.id()))) {
            throw new BadRequestException("Webhook does not belong to the configured Ekart account.");
        }
        // Managed external receivers must verify Ekart callbacks themselves. Zuufa callback activation
        // is deliberately unavailable until Ekart supplies its signing contract.
        return providerCall(() -> redactWebhook(api.saveWebhook(authorization(config(tenantId)), id,
                Map.of("url", request.url(), "secret", request.secret(), "topics", request.topics(), "active", request.active()))));
    }

    private EkartWebhookResponse redactWebhook(Map<String, Object> data) {
        if (data == null || !(data.get("id") instanceof String id)) throw new IllegalStateException();
        List<String> topics = data.get("topics") instanceof List<?> list ? list.stream().filter(String.class::isInstance).map(String.class::cast).toList() : List.of();
        return new EkartWebhookResponse(id, Objects.toString(data.get("url"), ""), topics, Boolean.TRUE.equals(data.get("active")));
    }

    private DeliveryProviderConfig config(UUID tenantId) {
        return configs.findByTenantIdAndProvider(tenantId, DeliveryProviderCode.EKART)
                .filter(c -> c.getEncryptedCredentials() != null)
                .orElseThrow(() -> new BadRequestException("Save Ekart credentials first."));
    }

    private <T> T providerCall(Supplier<T> call) {
        try { return call.get(); }
        catch (ServiceException e) { throw e; }
        catch (Exception e) { throw new ServiceException(502, "Ekart request failed. Refresh to check whether the change was saved before retrying."); }
    }
}
