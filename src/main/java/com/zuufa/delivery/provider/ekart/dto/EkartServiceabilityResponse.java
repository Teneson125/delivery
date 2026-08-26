package com.zuufa.delivery.provider.ekart.dto;

import java.math.BigDecimal;

public record EkartServiceabilityResponse(
        boolean serviceable,
        BigDecimal charge,
        Integer estimatedMinDays,
        Integer estimatedMaxDays,
        String message
) {
}
