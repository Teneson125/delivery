package com.zuufa.delivery.provider.ekart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EkartShipmentResponse(
        boolean status,
        String remark,
        @JsonProperty("tracking_id") String trackingId,
        String vendor
) {
}
