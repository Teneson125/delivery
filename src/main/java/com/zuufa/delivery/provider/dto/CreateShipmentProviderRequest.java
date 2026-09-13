package com.zuufa.delivery.provider.dto;

import com.zuufa.delivery.dto.DeliveryAddressRequest;
import com.zuufa.delivery.dto.DeliveryQuoteItemRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateShipmentProviderRequest(
        UUID tenantId,
        UUID orderId,
        BigDecimal subtotal,
        List<DeliveryQuoteItemRequest> items,
        DeliveryAddressRequest deliveryAddress
) {
}
