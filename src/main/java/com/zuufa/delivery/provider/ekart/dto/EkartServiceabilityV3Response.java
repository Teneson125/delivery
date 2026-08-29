package com.zuufa.delivery.provider.ekart.dto;

public record EkartServiceabilityV3Response(
        Tat tat,
        String courierGroup,
        Charges forwardDeliveredCharges
) {
    public record Tat(Integer min, Integer max) {
    }

    public record Charges(String totalForwardDeliveredEstimate) {
    }
}
