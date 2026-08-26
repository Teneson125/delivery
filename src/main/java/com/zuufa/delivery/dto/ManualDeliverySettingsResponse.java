package com.zuufa.delivery.dto;

import java.math.BigDecimal;

public record ManualDeliverySettingsResponse(
        boolean enabled,
        BigDecimal fixedCharge,
        BigDecimal freeDeliveryAbove,
        int estimatedMinDays,
        int estimatedMaxDays
) {
}
