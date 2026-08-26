package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import java.math.BigDecimal;

public record DeliveryQuoteOptionResponse(
        String quoteId,
        DeliveryProviderCode provider,
        String label,
        BigDecimal amount,
        boolean freeDeliveryApplied,
        int estimatedMinDays,
        int estimatedMaxDays,
        boolean serviceable,
        boolean recommended,
        String message
) {
}
