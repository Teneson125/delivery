package com.zuufa.delivery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryQuoteItemRequest(
        @NotNull UUID productId,
        @NotNull UUID variantId,
        @Min(1) int quantity,
        @Min(0) int weightGrams,
        @Min(0) int lengthCm,
        @Min(0) int widthCm,
        @Min(0) int heightCm,
        BigDecimal unitPrice
) {
}
