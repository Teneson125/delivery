package com.zuufa.delivery.provider.ekart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.provider.DeliveryProvider;
import com.zuufa.delivery.provider.dto.CreateShipmentProviderRequest;
import com.zuufa.delivery.provider.dto.DeliveryProviderContext;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderRequest;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderResponse;
import com.zuufa.delivery.provider.dto.ShipmentProviderResponse;
import com.zuufa.delivery.provider.dto.TrackShipmentProviderRequest;
import com.zuufa.delivery.provider.dto.TrackingProviderResponse;
import com.zuufa.delivery.provider.ekart.dto.EkartCredentials;
import com.zuufa.delivery.provider.ekart.dto.EkartEstimateRequest;
import com.zuufa.delivery.provider.ekart.dto.EkartEstimateResponse;
import com.zuufa.delivery.provider.ekart.dto.EkartServiceabilityRequest;
import com.zuufa.delivery.provider.ekart.dto.EkartServiceabilityV3Response;
import com.zuufa.delivery.provider.ekart.dto.EkartSettings;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EkartDeliveryProvider implements DeliveryProvider {

    private final EkartAuthClient authClient;
    private final EkartApiClient apiClient;
    private final ObjectMapper objectMapper;

    public EkartDeliveryProvider(
            EkartAuthClient authClient,
            EkartApiClient apiClient,
            ObjectMapper objectMapper
    ) {
        this.authClient = authClient;
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public DeliveryProviderCode code() {
        return DeliveryProviderCode.EKART;
    }

    @Override
    public DeliveryQuoteProviderResponse quote(DeliveryQuoteProviderRequest request, DeliveryProviderContext context) {
        if (!authClient.canAuthenticate(context)) {
            return unavailable("Ekart pricing is not enabled yet.");
        }

        try {
            EkartCredentials credentials = parseCredentials(context);
            EkartSettings settings = parseSettings(context);
            String pickupPincode = firstText(settings.pickupPincode(), context.pickupPincode());
            String dropPincode = onlyDigits(request.deliveryAddress().pincode());
            if (!StringUtils.hasText(pickupPincode) || !StringUtils.hasText(dropPincode)) {
                return unavailable("Ekart pickup or delivery pincode is missing.");
            }

            PackageMetrics metrics = packageMetrics(request);
            String authorization = authClient.getAuthorizationHeader(context, credentials);
            List<EkartServiceabilityV3Response> serviceability = apiClient.checkServiceability(
                    authorization,
                    new EkartServiceabilityRequest(
                            pickupPincode,
                            dropPincode,
                            String.valueOf(metrics.lengthCm()),
                            String.valueOf(metrics.heightCm()),
                            String.valueOf(metrics.widthCm()),
                            String.valueOf(metrics.weightGrams()),
                            paymentMode(settings),
                            serviceType(settings),
                            null,
                            request.subtotal().toPlainString()
                    )
            );
            if (serviceability == null || serviceability.isEmpty()) {
                return unavailable("Ekart is not serviceable for this address.");
            }

            EkartEstimateResponse estimate = apiClient.estimate(
                    authorization,
                    new EkartEstimateRequest(
                            Integer.valueOf(onlyDigits(pickupPincode)),
                            Integer.valueOf(dropPincode),
                            request.subtotal(),
                            metrics.weightGrams(),
                            metrics.lengthCm(),
                            metrics.heightCm(),
                            metrics.widthCm(),
                            serviceType(settings),
                            null
                    )
            );
            BigDecimal amount = parseAmount(estimate == null ? null : firstText(estimate.total(), estimate.shippingCharge()));
            EkartServiceabilityV3Response firstServiceable = serviceability.getFirst();
            return new DeliveryQuoteProviderResponse(
                    code(),
                    "Delivery",
                    amount,
                    false,
                    firstServiceable.tat() == null || firstServiceable.tat().min() == null ? 0 : firstServiceable.tat().min(),
                    firstServiceable.tat() == null || firstServiceable.tat().max() == null ? 0 : firstServiceable.tat().max(),
                    true,
                    null
            );
        } catch (RuntimeException error) {
            return unavailable("Ekart pricing is unavailable.");
        }
    }

    @Override
    public ShipmentProviderResponse createShipment(CreateShipmentProviderRequest request, DeliveryProviderContext context) {
        if (!authClient.canAuthenticate(context)) {
            return new ShipmentProviderResponse(null, null, "EKART_NOT_CONFIGURED");
        }

        return new ShipmentProviderResponse(null, null, "EKART_SHIPMENT_DETAILS_REQUIRED");
    }

    @Override
    public TrackingProviderResponse track(TrackShipmentProviderRequest request, DeliveryProviderContext context) {
        if (!authClient.canAuthenticate(context)) {
            return new TrackingProviderResponse("EKART_NOT_CONFIGURED", "Ekart tracking is not enabled yet.");
        }

        if (!StringUtils.hasText(request.providerShipmentId())) {
            return new TrackingProviderResponse("EKART_TRACKING_ID_REQUIRED", "Ekart tracking id is missing.");
        }

        try {
            Map<String, Object> response = apiClient.track(request.providerShipmentId());
            Object status = response == null ? null : response.get("status");
            return new TrackingProviderResponse(
                    status == null ? "EKART_TRACKING_RECEIVED" : status.toString(),
                    "Ekart tracking updated."
            );
        } catch (RuntimeException error) {
            return new TrackingProviderResponse("EKART_TRACKING_UNAVAILABLE", "Ekart tracking is unavailable.");
        }
    }

    @Override
    public void cancel(TrackShipmentProviderRequest request, DeliveryProviderContext context) {
        if (!authClient.canAuthenticate(context) || !StringUtils.hasText(request.providerShipmentId())) {
            return;
        }
        try {
            EkartCredentials credentials = parseCredentials(context);
            apiClient.cancel(authClient.getAuthorizationHeader(context, credentials), request.providerShipmentId());
        } catch (RuntimeException ignored) {
        }
    }

    private DeliveryQuoteProviderResponse unavailable(String message) {
        return new DeliveryQuoteProviderResponse(
                code(),
                "Ekart",
                null,
                false,
                0,
                0,
                false,
                message
        );
    }

    private EkartCredentials parseCredentials(DeliveryProviderContext context) {
        try {
            EkartCredentials credentials = objectMapper.readValue(context.encryptedCredentials(), EkartCredentials.class);
            if (!StringUtils.hasText(credentials.clientId())
                    || !StringUtils.hasText(credentials.username())
                    || !StringUtils.hasText(credentials.password())) {
                throw new IllegalStateException("Ekart credentials are incomplete");
            }
            return credentials;
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("Ekart credentials are invalid", error);
        }
    }

    private EkartSettings parseSettings(DeliveryProviderContext context) {
        if (!StringUtils.hasText(context.settingsJson())) {
            return new EkartSettings(null, null, "Prepaid", "SURFACE");
        }
        try {
            return objectMapper.readValue(context.settingsJson(), EkartSettings.class);
        } catch (JsonProcessingException error) {
            return new EkartSettings(null, null, "Prepaid", "SURFACE");
        }
    }

    private PackageMetrics packageMetrics(DeliveryQuoteProviderRequest request) {
        int weight = request.items().stream()
                .mapToInt(item -> Math.max(0, item.weightGrams()) * item.quantity())
                .sum();
        int length = request.items().stream().mapToInt(item -> Math.max(0, item.lengthCm())).max().orElse(0);
        int height = request.items().stream().mapToInt(item -> Math.max(0, item.heightCm())).max().orElse(0);
        int width = request.items().stream().mapToInt(item -> Math.max(0, item.widthCm())).max().orElse(0);
        return new PackageMetrics(
                weight > 0 ? weight : 500,
                length > 0 ? length : 10,
                height > 0 ? height : 10,
                width > 0 ? width : 10
        );
    }

    private String serviceType(EkartSettings settings) {
        return StringUtils.hasText(settings.serviceType()) ? settings.serviceType() : "SURFACE";
    }

    private String paymentMode(EkartSettings settings) {
        return StringUtils.hasText(settings.paymentMode()) ? settings.paymentMode() : "Prepaid";
    }

    private BigDecimal parseAmount(String value) {
        if (!StringUtils.hasText(value)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private String onlyDigits(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "");
    }

    private String firstText(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private record PackageMetrics(int weightGrams, int lengthCm, int heightCm, int widthCm) {
    }
}
