package com.zuufa.delivery.provider.ekart.dto;

public record EkartEstimateResponse(
        String shippingCharge,
        String total,
        String rid,
        String rSnapshotId
) {
}
