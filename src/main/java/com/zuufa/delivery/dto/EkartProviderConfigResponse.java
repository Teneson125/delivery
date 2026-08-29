package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;

public record EkartProviderConfigResponse(
        DeliveryProviderCode provider,
        boolean enabled,
        boolean configured,
        EkartProviderCredentialsResponse credentials,
        EkartProviderSettingsRequest settings
) {
}
