package com.zuufa.delivery.dto;

import java.util.List;

public record DeliveryFulfillmentOptionsResponse(
        List<DeliveryFulfillmentOptionResponse> providers
) {
}
