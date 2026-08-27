package com.zuufa.delivery.controller;

import static com.zuufa.common.authorization.ApplicationPermission.READ_DELIVERY_SETTINGS;
import static com.zuufa.common.authorization.ApplicationPermission.READ_WAREHOUSE;
import static com.zuufa.common.authorization.ApplicationPermission.UPDATE_DELIVERY_SETTINGS;
import static com.zuufa.common.authorization.ApplicationPermission.UPDATE_WAREHOUSE;
import static com.zuufa.common.authorization.ApplicationPermission.CREATE_ORDER;

import com.zuufa.delivery.dto.DeliverySettingsRequest;
import com.zuufa.delivery.dto.DeliverySettingsResponse;
import com.zuufa.delivery.dto.DeliveryQuoteRequest;
import com.zuufa.delivery.dto.DeliveryQuoteResponse;
import com.zuufa.delivery.dto.WarehouseRequest;
import com.zuufa.delivery.dto.WarehouseResponse;
import com.zuufa.delivery.service.DeliveryQuoteService;
import com.zuufa.delivery.service.DeliverySettingsService;
import com.zuufa.security.annotation.RequiredPermission;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery")
public class DeliverySettingsController {

    private final DeliverySettingsService deliverySettingsService;
    private final DeliveryQuoteService deliveryQuoteService;

    @GetMapping("/settings")
    // @RequiredPermission(READ_DELIVERY_SETTINGS)
    public DeliverySettingsResponse getSettings(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return deliverySettingsService.getSettings(tenantId);
    }

    @PutMapping("/settings")
    // @RequiredPermission(UPDATE_DELIVERY_SETTINGS)
    public DeliverySettingsResponse updateSettings(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody DeliverySettingsRequest request
    ) {
        return deliverySettingsService.updateSettings(tenantId, request);
    }

    @GetMapping("/warehouses/default")
    // @RequiredPermission(READ_WAREHOUSE)
    public WarehouseResponse getDefaultWarehouse(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return deliverySettingsService.getDefaultWarehouse(tenantId);
    }

    @PutMapping("/warehouses/default")
    // @RequiredPermission(UPDATE_WAREHOUSE)
    public WarehouseResponse saveDefaultWarehouse(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody WarehouseRequest request
    ) {
        return deliverySettingsService.saveDefaultWarehouse(tenantId, request);
    }

    @PostMapping("/quotes")
    // @RequiredPermission(CREATE_ORDER)
    public DeliveryQuoteResponse getQuotes(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody DeliveryQuoteRequest request
    ) {
        return deliveryQuoteService.getQuotes(tenantId, request);
    }
}
