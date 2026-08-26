package com.zuufa.delivery.provider.dto;

import java.util.UUID;

public record CreateShipmentProviderRequest(
        UUID tenantId,
        UUID orderId
) {
}
