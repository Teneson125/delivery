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

public interface DeliveryProvider {
    DeliveryProviderCode code();
    DeliveryQuoteProviderResponse quote(DeliveryQuoteProviderRequest request, DeliveryProviderContext context);
    ShipmentProviderResponse createShipment(CreateShipmentProviderRequest request, DeliveryProviderContext context);
    ShipmentLabelProviderResponse label(TrackShipmentProviderRequest request, DeliveryProviderContext context);
    TrackingProviderResponse track(TrackShipmentProviderRequest request, DeliveryProviderContext context);
    void cancel(TrackShipmentProviderRequest request, DeliveryProviderContext context);
}
