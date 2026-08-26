package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.ShipmentStatus;
import java.util.List;
import java.util.UUID;

public record TrackingResponse(
        UUID shipmentId,
        UUID orderId,
        DeliveryProviderCode provider,
        ShipmentStatus status,
        String trackingNumber,
        List<ShipmentEventResponse> events
) {
}
