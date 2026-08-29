package com.zuufa.delivery.provider.ekart.dto;

public record EkartSettings(
        String pickupPincode,
        String pickupAddressAlias,
        String paymentMode,
        String serviceType
) {
}
