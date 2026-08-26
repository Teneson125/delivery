package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.ShipmentStatus;
import java.time.Instant;

public record ShipmentEventResponse(
        ShipmentStatus status,
        String message,
        Instant occurredAt
) {
}
