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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery/providers/ekart")
public class DeliveryProviderSettingsController {

    private final DeliveryProviderSettingsService providerSettingsService;

    @GetMapping
    public EkartProviderConfigResponse getEkartConfig(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return providerSettingsService.getEkartConfig(tenantId);
    }

    @PutMapping
    public EkartProviderConfigResponse saveEkartConfig(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody EkartProviderConfigRequest request
    ) {
        return providerSettingsService.saveEkartConfig(tenantId, request);
    }

    @PostMapping("/test")
    public ProviderConnectionTestResponse testEkartConnection(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return providerSettingsService.testEkartConnection(tenantId);
    }
}
