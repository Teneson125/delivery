package com.zuufa.delivery.dto;

import java.math.BigDecimal;
import java.util.List;

public record DeliveryQuoteResponse(
        String currency,
        List<DeliveryQuoteOptionResponse> quotes,
        BigDecimal deliveryCharge,
        boolean freeDeliveryApplied,
        Integer estimatedMinDays,
        Integer estimatedMaxDays,
        boolean serviceable
) {
    public DeliveryQuoteResponse(String currency, List<DeliveryQuoteOptionResponse> quotes) {
        this(currency, quotes, selectedQuote(quotes));
    }

    private DeliveryQuoteResponse(
            String currency,
            List<DeliveryQuoteOptionResponse> quotes,
            DeliveryQuoteOptionResponse selectedQuote
    ) {
        this(
                currency,
                quotes,
                selectedQuote == null || selectedQuote.amount() == null ? BigDecimal.ZERO : selectedQuote.amount(),
                selectedQuote != null && selectedQuote.freeDeliveryApplied(),
                selectedQuote == null || selectedQuote.estimatedMinDays() <= 0 ? null : selectedQuote.estimatedMinDays(),
                selectedQuote == null || selectedQuote.estimatedMaxDays() <= 0 ? null : selectedQuote.estimatedMaxDays(),
                selectedQuote != null && selectedQuote.serviceable()
        );
    }

    private static DeliveryQuoteOptionResponse selectedQuote(List<DeliveryQuoteOptionResponse> quotes) {
        if (quotes == null) {
            return null;
        }

        return quotes.stream()
                .filter(DeliveryQuoteOptionResponse::recommended)
                .findFirst()
                .or(() -> quotes.stream().filter(DeliveryQuoteOptionResponse::serviceable).findFirst())
                .orElse(null);
    }
}
