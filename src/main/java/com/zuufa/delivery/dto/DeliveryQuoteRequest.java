package com.zuufa.delivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record DeliveryQuoteRequest(
        BigDecimal subtotal,
        @NotEmpty List<@Valid DeliveryQuoteItemRequest> items,
        @Valid @NotNull DeliveryAddressRequest deliveryAddress
) {
}
