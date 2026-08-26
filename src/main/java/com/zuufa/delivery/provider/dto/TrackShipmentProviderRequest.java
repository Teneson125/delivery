package com.zuufa.delivery.provider.dto;

import java.util.UUID;

public record TrackShipmentProviderRequest(
        UUID tenantId,
        String providerShipmentId,
        String trackingNumber
) {
}
