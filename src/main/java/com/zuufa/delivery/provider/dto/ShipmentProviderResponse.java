package com.zuufa.delivery.provider.dto;

public record ShipmentProviderResponse(
        String providerShipmentId,
        String trackingNumber,
        String status
) {
}
