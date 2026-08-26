package com.zuufa.delivery.entity;

import com.zuufa.common.entity.AbstractAuditEntity;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.ShipmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "shipments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_shipments_tenant_order",
                columnNames = {"tenant_id", "order_id"}
        )
)
public class Shipment extends AbstractAuditEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private DeliveryProviderCode provider;

    @Column(name = "provider_shipment_id")
    private String providerShipmentId;

    @Column(name = "awb_number")
    private String awbNumber;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Column(name = "delivery_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(nullable = false, length = 8)
    private String currency = "INR";

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "estimated_min_days")
    private Integer estimatedMinDays;

    @Column(name = "estimated_max_days")
    private Integer estimatedMaxDays;
}
