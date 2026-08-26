package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;

public record ProviderConfigResponse(
        DeliveryProviderCode provider,
        boolean enabled,
        boolean configured,
        int priority
) {
}
