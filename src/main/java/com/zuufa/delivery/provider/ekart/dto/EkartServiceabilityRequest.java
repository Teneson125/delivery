package com.zuufa.delivery.provider.ekart.dto;

public record EkartServiceabilityRequest(
        String pickupPostalCode,
        String deliveryPostalCode,
        long weightInGrams
) {
}
