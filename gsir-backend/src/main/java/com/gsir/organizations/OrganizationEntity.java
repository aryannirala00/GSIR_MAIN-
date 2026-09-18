package com.gsir.organizations;

import com.gsir.common.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity(name = "Organization")
@Table(name = "organizations")
public class OrganizationEntity extends AuditedEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    protected OrganizationEntity() {}

    public OrganizationEntity(UUID id, String name, UUID createdBy) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }
}

