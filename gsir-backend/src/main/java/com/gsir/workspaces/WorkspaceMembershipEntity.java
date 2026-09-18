package com.gsir.workspaces;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "WorkspaceMembership")
@Table(name = "workspace_memberships")
public class WorkspaceMembershipEntity {

    @EmbeddedId
    private WorkspaceMembershipId id;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected WorkspaceMembershipEntity() {}

    public WorkspaceMembershipEntity(UUID workspaceId, UUID userId, String role) {
        this.id = new WorkspaceMembershipId(workspaceId, userId);
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public WorkspaceMembershipId getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return id != null ? id.getWorkspaceId() : null;
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

