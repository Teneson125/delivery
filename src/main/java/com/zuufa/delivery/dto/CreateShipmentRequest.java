package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateShipmentRequest(
        @NotNull UUID orderId,
        DeliveryProviderCode provider,
        @PositiveOrZero BigDecimal deliveryCharge,
        String currency,
        UUID warehouseId,
        Integer estimatedMinDays,
        Integer estimatedMaxDays
) {
}
