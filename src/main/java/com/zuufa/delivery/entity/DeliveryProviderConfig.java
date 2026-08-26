package com.zuufa.delivery.entity;

import com.zuufa.common.entity.AbstractAuditEntity;
import com.zuufa.delivery.enums.DeliveryProviderCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "delivery_provider_configs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_delivery_provider_configs_tenant_provider",
                columnNames = {"tenant_id", "provider"}
        )
)
public class DeliveryProviderConfig extends AbstractAuditEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private DeliveryProviderCode provider;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private int priority = 100;

    @Lob
    @Column(name = "encrypted_credentials")
    private String encryptedCredentials;

    @Lob
    @Column(name = "settings_json")
    private String settingsJson;
}
