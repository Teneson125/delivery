package com.zuufa.delivery.repository;

import com.zuufa.delivery.entity.TenantDeliverySettings;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantDeliverySettingsRepository extends JpaRepository<TenantDeliverySettings, UUID> {
    Optional<TenantDeliverySettings> findByTenantId(UUID tenantId);
}
