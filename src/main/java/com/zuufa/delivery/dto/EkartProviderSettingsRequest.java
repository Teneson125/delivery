package com.zuufa.delivery.dto;

public record EkartProviderSettingsRequest(
        String pickupPincode,
        String pickupAddressAlias,
        String returnAddressAlias,
        String paymentMode,
        String serviceType
) {
}
