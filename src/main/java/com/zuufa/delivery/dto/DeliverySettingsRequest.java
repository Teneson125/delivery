package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DeliverySettingsRequest(
        boolean enabled,
        @NotNull DeliveryProviderCode defaultProvider,
        @Valid @NotNull ManualDeliverySettingsRequest manual
) {
}
