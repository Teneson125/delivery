package com.zuufa.delivery.provider.ekart.dto;

public record EkartShipmentRequest(
        String orderId,
        String pickupAddressId,
        String deliveryPostalCode
) {
}
