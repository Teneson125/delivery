package com.zuufa.delivery.provider.ekart.dto;

public record EkartTrackingResponse(
        String trackingNumber,
        String status,
        String message
) {
}
