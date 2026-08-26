package com.zuufa.delivery.service;

import com.zuufa.delivery.dto.DeliverySettingsRequest;
import com.zuufa.delivery.dto.DeliverySettingsResponse;
import com.zuufa.delivery.dto.WarehouseRequest;
import com.zuufa.delivery.dto.WarehouseResponse;
import java.util.UUID;

public interface DeliverySettingsService {
    DeliverySettingsResponse getSettings(UUID tenantId);
    DeliverySettingsResponse updateSettings(UUID tenantId, DeliverySettingsRequest request);
    WarehouseResponse getDefaultWarehouse(UUID tenantId);
    WarehouseResponse saveDefaultWarehouse(UUID tenantId, WarehouseRequest request);
}
