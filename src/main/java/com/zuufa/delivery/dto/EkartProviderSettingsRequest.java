package com.zuufa.delivery.dto;

public record EkartProviderSettingsRequest(
        String pickupPincode,
        String pickupAddressAlias,
        String paymentMode,
        String serviceType
) {
}
