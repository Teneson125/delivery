package com.zuufa.delivery.provider.ekart.dto;

public record EkartShipmentResponse(
        String shipmentId,
        String trackingNumber,
        String status
) {
}
