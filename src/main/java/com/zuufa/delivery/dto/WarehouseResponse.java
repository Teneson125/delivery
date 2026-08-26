package com.zuufa.delivery.dto;

import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        UUID tenantId,
        String name,
        String contactName,
        String phoneNumber,
        String email,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String pincode,
        boolean defaultWarehouse,
        boolean active
) {
}
