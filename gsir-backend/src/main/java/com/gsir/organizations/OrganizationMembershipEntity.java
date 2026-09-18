package com.gsir.organizations;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "OrganizationMembership")
@Table(name = "organization_memberships")
public class OrganizationMembershipEntity {

    @EmbeddedId
    private OrganizationMembershipId id;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected OrganizationMembershipEntity() {}

    public OrganizationMembershipEntity(UUID organizationId, UUID userId, String role) {
        this.id = new OrganizationMembershipId(organizationId, userId);
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public OrganizationMembershipId getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return id != null ? id.getOrganizationId() : null;
    }

    public UUID getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public String getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

