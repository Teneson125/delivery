package com.zuufa.delivery.repository;

import com.zuufa.delivery.entity.Warehouse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Optional<Warehouse> findByTenantIdAndDefaultWarehouseTrue(UUID tenantId);
}
