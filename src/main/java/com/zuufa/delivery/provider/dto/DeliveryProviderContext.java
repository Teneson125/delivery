package com.zuufa.delivery.provider.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryProviderContext(
        UUID tenantId,
        String encryptedCredentials,
        String settingsJson,
        String pickupPincode,
        BigDecimal manualFixedCharge,
        BigDecimal manualFreeDeliveryAbove,
        int manualEstimatedMinDays,
        int manualEstimatedMaxDays
) {
}
