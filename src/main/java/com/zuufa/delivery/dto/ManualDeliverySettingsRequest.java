package com.zuufa.delivery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record ManualDeliverySettingsRequest(
        boolean enabled,
        @DecimalMin(value = "0.00") BigDecimal fixedCharge,
        @DecimalMin(value = "0.00") BigDecimal freeDeliveryAbove,
        @Min(0) int estimatedMinDays,
        @Min(0) int estimatedMaxDays
) {
}
