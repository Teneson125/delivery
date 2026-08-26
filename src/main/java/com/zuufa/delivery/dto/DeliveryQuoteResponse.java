package com.zuufa.delivery.dto;

import java.util.List;

public record DeliveryQuoteResponse(
        String currency,
        List<DeliveryQuoteOptionResponse> quotes
) {
}
