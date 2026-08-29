package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import java.util.Map;
import java.util.UUID;

public record ShipmentLabelResponse(
        UUID shipmentId,
        DeliveryProviderCode provider,
        String labelUrl,
        boolean available,
        String message,
        Map<String, Object> data
) {
}
