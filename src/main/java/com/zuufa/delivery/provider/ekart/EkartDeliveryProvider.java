package com.zuufa.delivery.provider.ekart;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.provider.DeliveryProvider;
import com.zuufa.delivery.provider.dto.CreateShipmentProviderRequest;
import com.zuufa.delivery.provider.dto.DeliveryProviderContext;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderRequest;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderResponse;
import com.zuufa.delivery.provider.dto.ShipmentProviderResponse;
import com.zuufa.delivery.provider.dto.TrackShipmentProviderRequest;
import com.zuufa.delivery.provider.dto.TrackingProviderResponse;
import org.springframework.stereotype.Component;

@Component
public class EkartDeliveryProvider implements DeliveryProvider {

    private final EkartAuthClient authClient;

    public EkartDeliveryProvider(EkartAuthClient authClient) {
        this.authClient = authClient;
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

        return unavailable("Ekart live quote integration is pending API verification.");
    }

    @Override
    public ShipmentProviderResponse createShipment(CreateShipmentProviderRequest request, DeliveryProviderContext context) {
        if (!authClient.canAuthenticate(context)) {
            return new ShipmentProviderResponse(null, null, "EKART_NOT_CONFIGURED");
        }

        return new ShipmentProviderResponse(null, null, "EKART_PENDING_INTEGRATION");
    }

    @Override
    public TrackingProviderResponse track(TrackShipmentProviderRequest request, DeliveryProviderContext context) {
        if (!authClient.canAuthenticate(context)) {
            return new TrackingProviderResponse("EKART_NOT_CONFIGURED", "Ekart tracking is not enabled yet.");
        }

        return new TrackingProviderResponse("EKART_PENDING_INTEGRATION", "Ekart tracking integration is pending.");
    }

    @Override
    public void cancel(TrackShipmentProviderRequest request, DeliveryProviderContext context) {
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
}
