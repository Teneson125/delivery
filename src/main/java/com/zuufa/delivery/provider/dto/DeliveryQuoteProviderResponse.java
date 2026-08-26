package com.zuufa.delivery.provider.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import java.math.BigDecimal;

public record DeliveryQuoteProviderResponse(
        DeliveryProviderCode provider,
        String label,
        BigDecimal amount,
        boolean freeDeliveryApplied,
        int estimatedMinDays,
        int estimatedMaxDays,
        boolean serviceable,
        String message
) {
}
