package com.zuufa.delivery.entity;

import com.zuufa.common.entity.AbstractAuditEntity;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import com.zuufa.delivery.enums.DeliveryPricingStrategy;
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
        name = "tenant_delivery_settings",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenant_delivery_settings_tenant", columnNames = "tenant_id")
)
public class TenantDeliverySettings extends AbstractAuditEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_provider", nullable = false, length = 64)
    private DeliveryProviderCode defaultProvider = DeliveryProviderCode.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_strategy", length = 32)
    private DeliveryPricingStrategy pricingStrategy = DeliveryPricingStrategy.HIGHEST;

    @Column(name = "manual_enabled", nullable = false)
    private boolean manualEnabled = true;

    @Column(name = "manual_fixed_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal manualFixedCharge = BigDecimal.valueOf(80);

    @Column(name = "manual_free_delivery_above", precision = 12, scale = 2)
    private BigDecimal manualFreeDeliveryAbove;

    @Column(name = "manual_estimated_min_days", nullable = false)
    private int manualEstimatedMinDays = 2;

    @Column(name = "manual_estimated_max_days", nullable = false)
    private int manualEstimatedMaxDays = 5;
}
