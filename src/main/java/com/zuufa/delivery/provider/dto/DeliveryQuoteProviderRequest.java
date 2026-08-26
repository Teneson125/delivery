package com.zuufa.delivery.provider.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryQuoteProviderRequest(
        UUID tenantId,
        BigDecimal subtotal
) {
}
