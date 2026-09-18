package com.zuufa.delivery.controller;

import com.zuufa.delivery.dto.EkartProviderConfigRequest;
import com.zuufa.delivery.dto.EkartProviderConfigResponse;
import com.zuufa.delivery.dto.ProviderConnectionTestResponse;
import com.zuufa.delivery.service.DeliveryProviderSettingsService;
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
import org.springframework.web.bind.annotation.PathVariable;
import com.zuufa.security.annotation.RequiredPermission;
import static com.zuufa.common.authorization.ApplicationPermission.READ_DELIVERY_SETTINGS;
import static com.zuufa.common.authorization.ApplicationPermission.UPDATE_DELIVERY_SETTINGS;
import com.zuufa.delivery.dto.*;
import com.zuufa.delivery.service.EkartAccountService;
import com.zuufa.delivery.provider.ekart.dto.EkartAddress;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery/providers/ekart")
public class DeliveryProviderSettingsController {

    private final DeliveryProviderSettingsService providerSettingsService;
    private final EkartAccountService accountService;

    @GetMapping
    @RequiredPermission(READ_DELIVERY_SETTINGS)
    public EkartProviderConfigResponse getEkartConfig(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return providerSettingsService.getEkartConfig(tenantId);
    }

    @PutMapping
    @RequiredPermission(UPDATE_DELIVERY_SETTINGS)
    public EkartProviderConfigResponse saveEkartConfig(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody EkartProviderConfigRequest request
    ) {
        return providerSettingsService.saveEkartConfig(tenantId, request);
    }

    @PostMapping("/test")
    @RequiredPermission(UPDATE_DELIVERY_SETTINGS)
    public ProviderConnectionTestResponse testEkartConnection(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return providerSettingsService.testEkartConnection(tenantId);
    }

    @GetMapping("/addresses")
    @RequiredPermission(READ_DELIVERY_SETTINGS)
    public List<EkartAddress> addresses(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return accountService.addresses(tenantId);
    }

    @PostMapping("/addresses")
    @RequiredPermission(UPDATE_DELIVERY_SETTINGS)
    public List<EkartAddress> addAddress(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody EkartAddressRequest request) {
        return accountService.addAddress(tenantId, request);
    }

    @GetMapping("/webhooks")
    @RequiredPermission(READ_DELIVERY_SETTINGS)
    public List<EkartWebhookResponse> webhooks(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return accountService.webhooks(tenantId);
    }

    @PostMapping("/webhooks")
    @RequiredPermission(UPDATE_DELIVERY_SETTINGS)
    public EkartWebhookResponse addWebhook(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody EkartWebhookRequest request) {
        return accountService.saveWebhook(tenantId, null, request);
    }

    @PutMapping("/webhooks/{id}")
    @RequiredPermission(UPDATE_DELIVERY_SETTINGS)
    public EkartWebhookResponse updateWebhook(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable String id,
            @Valid @RequestBody EkartWebhookRequest request) {
        return accountService.saveWebhook(tenantId, id, request);
    }
}
