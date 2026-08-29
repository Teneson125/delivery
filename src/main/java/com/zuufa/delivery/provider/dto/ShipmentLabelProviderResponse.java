package com.zuufa.delivery.provider.dto;

import java.util.Map;

public record ShipmentLabelProviderResponse(
        String labelUrl,
        boolean available,
        String message,
        Map<String, Object> data
) {
}
