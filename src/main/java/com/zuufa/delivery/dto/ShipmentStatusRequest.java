package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record ShipmentStatusRequest(
        @NotNull ShipmentStatus status,
        String message
) {
}
