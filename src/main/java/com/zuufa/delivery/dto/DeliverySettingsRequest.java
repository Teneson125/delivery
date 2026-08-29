package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.DeliveryPricingStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DeliverySettingsRequest(
        boolean enabled,
        @NotNull DeliveryProviderCode defaultProvider,
        DeliveryPricingStrategy pricingStrategy,
        @Valid @NotNull ManualDeliverySettingsRequest manual
) {
}
