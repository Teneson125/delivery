package com.zuufa.delivery.entity;

import com.zuufa.common.entity.AbstractAuditEntity;
import com.zuufa.delivery.enums.ShipmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shipment_events")
public class ShipmentEvent extends AbstractAuditEntity {

    @Column(name = "shipment_id", nullable = false)
    private UUID shipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ShipmentStatus status;

    @Column(length = 500)
    private String message;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();
}
