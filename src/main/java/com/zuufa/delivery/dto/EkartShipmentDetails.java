package com.zuufa.delivery.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Actual invoice values supplied by the booking caller; never inferred from a cart subtotal. */
public record EkartShipmentDetails(
        @NotBlank String sellerName, @NotBlank String sellerAddress, @NotNull String sellerGstTin,
        @NotBlank String invoiceNumber, @NotNull LocalDate invoiceDate,
        @NotBlank String categoryOfGoods, @NotBlank String productsDescription,
        @NotNull @DecimalMin("1") BigDecimal totalAmount,
        @NotNull @PositiveOrZero BigDecimal taxValue,
        @NotNull @DecimalMin("1") BigDecimal taxableAmount,
        @NotNull @PositiveOrZero BigDecimal consigneeGstAmount,
        @NotNull @Pattern(regexp = "Prepaid|COD") String paymentMode,
        @NotNull @DecimalMin("0") @DecimalMax("49999") BigDecimal codAmount,
        @Positive int weightGrams, @Positive int lengthCm, @Positive int widthCm, @Positive int heightCm
) {}
