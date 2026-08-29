package com.zuufa.delivery.provider;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.provider.dto.CreateShipmentProviderRequest;
import com.zuufa.delivery.provider.dto.DeliveryProviderContext;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderRequest;
import com.zuufa.delivery.provider.dto.DeliveryQuoteProviderResponse;
import com.zuufa.delivery.provider.dto.ShipmentLabelProviderResponse;
import com.zuufa.delivery.provider.dto.ShipmentProviderResponse;
import com.zuufa.delivery.provider.dto.TrackShipmentProviderRequest;
import com.zuufa.delivery.provider.dto.TrackingProviderResponse;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ManualDeliveryProvider implements DeliveryProvider {

    @Override
    public DeliveryProviderCode code() {
        return DeliveryProviderCode.MANUAL;
    }

    @Override
    public DeliveryQuoteProviderResponse quote(DeliveryQuoteProviderRequest request, DeliveryProviderContext context) {
        BigDecimal amount = context.manualFixedCharge();
        boolean freeDelivery = context.manualFreeDeliveryAbove() != null
                && request.subtotal().compareTo(context.manualFreeDeliveryAbove()) >= 0;
        if (freeDelivery) {
            amount = BigDecimal.ZERO;
        }
        return new DeliveryQuoteProviderResponse(
                code(),
                "Store-managed delivery",
                amount,
                freeDelivery,
                context.manualEstimatedMinDays(),
                context.manualEstimatedMaxDays(),
                true,
                null
        );
    }

    @Override
    public ShipmentProviderResponse createShipment(CreateShipmentProviderRequest request, DeliveryProviderContext context) {
        return new ShipmentProviderResponse(null, null, "READY_TO_SHIP");
    }

    @Override
    public ShipmentLabelProviderResponse label(TrackShipmentProviderRequest request, DeliveryProviderContext context) {
        return new ShipmentLabelProviderResponse(null, false, "Manual delivery does not have a provider label.", Map.of());
    }

    @Override
    public TrackingProviderResponse track(TrackShipmentProviderRequest request, DeliveryProviderContext context) {
        return new TrackingProviderResponse("READY_TO_SHIP", "Store-managed delivery");
    }

    @Override
    public void cancel(TrackShipmentProviderRequest request, DeliveryProviderContext context) {
    }
}
