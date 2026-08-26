package com.zuufa.delivery.dto;

import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.ShipmentStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        UUID tenantId,
        UUID orderId,
        DeliveryProviderCode provider,
        String providerShipmentId,
        String awbNumber,
        String trackingNumber,
        ShipmentStatus status,
        BigDecimal deliveryCharge,
        String currency,
        UUID warehouseId,
        Integer estimatedMinDays,
        Integer estimatedMaxDays,
        List<ShipmentEventResponse> events
) {
}
