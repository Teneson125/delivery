package com.zuufa.delivery.provider.ekart.dto;

public record EkartServiceabilityRequest(
        String pickupPincode,
        String dropPincode,
        String length,
        String height,
        String width,
        String weight,
        String paymentType,
        String serviceType,
        String codAmount,
        String invoiceAmount
) {
}
