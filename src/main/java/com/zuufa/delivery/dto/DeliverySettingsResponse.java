package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.DeliveryPricingStrategy;
import java.util.List;
import java.util.UUID;

public record DeliverySettingsResponse(
        UUID tenantId,
        boolean enabled,
        DeliveryProviderCode defaultProvider,
        DeliveryPricingStrategy pricingStrategy,
        ManualDeliverySettingsResponse manual,
        List<ProviderConfigResponse> providers
) {
}
