package com.zuufa.delivery.service.impl;

import com.zuufa.delivery.dto.DeliverySettingsRequest;
import com.zuufa.delivery.dto.DeliverySettingsResponse;
import com.zuufa.delivery.dto.ManualDeliverySettingsResponse;
import com.zuufa.delivery.dto.ProviderConfigResponse;
import com.zuufa.delivery.dto.WarehouseRequest;
import com.zuufa.delivery.dto.WarehouseResponse;
import com.zuufa.delivery.entity.DeliveryProviderConfig;
import com.zuufa.delivery.entity.TenantDeliverySettings;
import com.zuufa.delivery.entity.Warehouse;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.repository.DeliveryProviderConfigRepository;
import com.zuufa.delivery.repository.TenantDeliverySettingsRepository;
import com.zuufa.delivery.repository.WarehouseRepository;
import com.zuufa.delivery.service.DeliverySettingsService;
import com.zuufa.exception.BadRequestException;
import com.zuufa.exception.NotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliverySettingsServiceImpl implements DeliverySettingsService {

    private final TenantDeliverySettingsRepository settingsRepository;
    private final WarehouseRepository warehouseRepository;
    private final DeliveryProviderConfigRepository providerConfigRepository;

    @Override
    @Transactional
    public DeliverySettingsResponse getSettings(UUID tenantId) {
        TenantDeliverySettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> settingsRepository.save(defaultSettings(tenantId)));
        ensureDefaultProviderConfigs(tenantId);
        return toResponse(settings);
    }

    @Override
    @Transactional
    public DeliverySettingsResponse updateSettings(UUID tenantId, DeliverySettingsRequest request) {
        if (request.manual().estimatedMaxDays() < request.manual().estimatedMinDays()) {
            throw new BadRequestException("Estimated max days must be greater than or equal to min days");
        }
        TenantDeliverySettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> defaultSettings(tenantId));
        settings.setEnabled(request.enabled());
        settings.setDefaultProvider(request.defaultProvider());
        settings.setManualEnabled(request.manual().enabled());
        settings.setManualFixedCharge(request.manual().fixedCharge());
        settings.setManualFreeDeliveryAbove(request.manual().freeDeliveryAbove());
        settings.setManualEstimatedMinDays(request.manual().estimatedMinDays());
        settings.setManualEstimatedMaxDays(request.manual().estimatedMaxDays());
        settingsRepository.save(settings);
        ensureDefaultProviderConfigs(tenantId);
        return toResponse(settings);
    }

    @Override
    public WarehouseResponse getDefaultWarehouse(UUID tenantId) {
        return warehouseRepository.findByTenantIdAndDefaultWarehouseTrue(tenantId)
                .map(this::toWarehouseResponse)
                .orElseThrow(() -> new NotFoundException("Default warehouse not found"));
    }

    @Override
    @Transactional
    public WarehouseResponse saveDefaultWarehouse(UUID tenantId, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findByTenantIdAndDefaultWarehouseTrue(tenantId)
                .orElseGet(Warehouse::new);
        warehouse.setTenantId(tenantId);
        warehouse.setName(request.name());
        warehouse.setContactName(request.contactName());
        warehouse.setPhoneNumber(request.phoneNumber());
        warehouse.setEmail(request.email());
        warehouse.setAddressLine1(request.addressLine1());
        warehouse.setAddressLine2(request.addressLine2());
        warehouse.setCity(request.city());
        warehouse.setState(request.state());
        warehouse.setCountry(request.country());
        warehouse.setPincode(request.pincode());
        warehouse.setDefaultWarehouse(true);
        warehouse.setActive(true);
        return toWarehouseResponse(warehouseRepository.save(warehouse));
    }

    private TenantDeliverySettings defaultSettings(UUID tenantId) {
        TenantDeliverySettings settings = new TenantDeliverySettings();
        settings.setTenantId(tenantId);
        return settings;
    }

    private void ensureDefaultProviderConfigs(UUID tenantId) {
        Arrays.stream(DeliveryProviderCode.values()).forEach(provider -> {
            providerConfigRepository.findByTenantIdAndProvider(tenantId, provider).orElseGet(() -> {
                DeliveryProviderConfig config = new DeliveryProviderConfig();
                config.setTenantId(tenantId);
                config.setProvider(provider);
                config.setEnabled(provider == DeliveryProviderCode.MANUAL);
                config.setPriority(provider == DeliveryProviderCode.MANUAL ? 1 : 100);
                return providerConfigRepository.save(config);
            });
        });
    }

    private DeliverySettingsResponse toResponse(TenantDeliverySettings settings) {
        List<ProviderConfigResponse> providers = providerConfigRepository
                .findByTenantIdOrderByPriorityAsc(settings.getTenantId())
                .stream()
                .map(config -> new ProviderConfigResponse(
                        config.getProvider(),
                        config.isEnabled(),
                        config.getProvider() == DeliveryProviderCode.MANUAL || config.getEncryptedCredentials() != null,
                        config.getPriority()
                ))
                .toList();
        return new DeliverySettingsResponse(
                settings.getTenantId(),
                settings.isEnabled(),
                settings.getDefaultProvider(),
                new ManualDeliverySettingsResponse(
                        settings.isManualEnabled(),
                        settings.getManualFixedCharge(),
                        settings.getManualFreeDeliveryAbove(),
                        settings.getManualEstimatedMinDays(),
                        settings.getManualEstimatedMaxDays()
                ),
                providers
        );
    }

    private WarehouseResponse toWarehouseResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getTenantId(),
                warehouse.getName(),
                warehouse.getContactName(),
                warehouse.getPhoneNumber(),
                warehouse.getEmail(),
                warehouse.getAddressLine1(),
                warehouse.getAddressLine2(),
                warehouse.getCity(),
                warehouse.getState(),
                warehouse.getCountry(),
                warehouse.getPincode(),
                warehouse.isDefaultWarehouse(),
                warehouse.isActive()
        );
    }
}
