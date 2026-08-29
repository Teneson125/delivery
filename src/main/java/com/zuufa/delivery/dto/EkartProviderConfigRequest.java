package com.zuufa.delivery.dto;

import jakarta.validation.Valid;

public record EkartProviderConfigRequest(
        boolean enabled,
        @Valid EkartProviderCredentialsRequest credentials,
        EkartProviderSettingsRequest settings
) {
}
