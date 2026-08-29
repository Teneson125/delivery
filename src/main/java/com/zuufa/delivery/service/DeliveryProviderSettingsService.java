package com.zuufa.delivery.service;

import com.zuufa.delivery.dto.EkartProviderConfigRequest;
import com.zuufa.delivery.dto.EkartProviderConfigResponse;
import com.zuufa.delivery.dto.ProviderConnectionTestResponse;
import java.util.UUID;

public interface DeliveryProviderSettingsService {
    EkartProviderConfigResponse getEkartConfig(UUID tenantId);
    EkartProviderConfigResponse saveEkartConfig(UUID tenantId, EkartProviderConfigRequest request);
    ProviderConnectionTestResponse testEkartConnection(UUID tenantId);
}
