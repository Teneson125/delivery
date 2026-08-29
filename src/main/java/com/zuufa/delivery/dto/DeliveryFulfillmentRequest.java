package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DeliveryFulfillmentRequest(
        @NotNull DeliveryProviderCode provider,
        ShipmentStatus status,
        String trackingNumber,
        String trackingUrl,
        String note,
        LocalDate estimatedDeliveryDate
) {
}
