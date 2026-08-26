package com.zuufa.delivery.entity;

import com.zuufa.common.entity.AbstractAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "warehouses",
        uniqueConstraints = @UniqueConstraint(name = "uk_warehouses_tenant_default", columnNames = {"tenant_id", "default_warehouse"})
)
public class Warehouse extends AbstractAuditEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "contact_name", nullable = false, length = 120)
    private String contactName;

    @Column(name = "phone_number", nullable = false, length = 32)
    private String phoneNumber;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(name = "address_line_1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line_2", length = 255)
    private String addressLine2;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 120)
    private String state;

    @Column(nullable = false, length = 2)
    private String country = "IN";

    @Column(nullable = false, length = 16)
    private String pincode;

    @Column(name = "default_warehouse", nullable = false)
    private boolean defaultWarehouse = true;

    @Column(nullable = false)
    private boolean active = true;
}
