package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import java.util.List;
import java.util.UUID;

public record DeliverySettingsResponse(
        UUID tenantId,
        boolean enabled,
        DeliveryProviderCode defaultProvider,
        ManualDeliverySettingsResponse manual,
        List<ProviderConfigResponse> providers
) {
}
