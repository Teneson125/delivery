package com.zuufa.delivery.repository;

import com.zuufa.delivery.entity.Shipment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    List<Shipment> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    Optional<Shipment> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<Shipment> findByTenantIdAndOrderId(UUID tenantId, UUID orderId);
}
