package com.zuufa.delivery.repository;

import com.zuufa.delivery.entity.DeliveryProviderConfig;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryProviderConfigRepository extends JpaRepository<DeliveryProviderConfig, UUID> {
    List<DeliveryProviderConfig> findByTenantIdOrderByPriorityAsc(UUID tenantId);
    Optional<DeliveryProviderConfig> findByTenantIdAndProvider(UUID tenantId, DeliveryProviderCode provider);
}
