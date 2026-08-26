package com.zuufa.delivery.provider.ekart.dto;

public record EkartCancelShipmentRequest(
        String shipmentId,
        String reason
) {
}
