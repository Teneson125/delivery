package com.zuufa.delivery.provider.ekart.dto;

import java.math.BigDecimal;

public record EkartEstimateRequest(
        Integer pickupPincode,
        Integer dropPincode,
        BigDecimal invoiceAmount,
        Integer weight,
        Integer length,
        Integer height,
        Integer width,
        String serviceType,
        BigDecimal codAmount
) {
}
