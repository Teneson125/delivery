package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;

public record DeliveryFulfillmentOptionResponse(
        DeliveryProviderCode provider,
        String displayName,
        boolean available,
        boolean configured
) {
}
