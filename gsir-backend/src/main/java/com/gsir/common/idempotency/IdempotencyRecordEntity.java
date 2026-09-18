package com.gsir.common.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity(name = "IdempotencyRecord")
@Table(name = "idempotency_records")
public class IdempotencyRecordEntity implements Persistable<IdempotencyRecordId> {

    @EmbeddedId
    private IdempotencyRecordId id;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IdempotencyRecordEntity() {}

    public IdempotencyRecordEntity(IdempotencyRecordId id, String resourceType, UUID resourceId) {
        this.id = id;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public IdempotencyRecordId getId() {
        return id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    @Transient
    public boolean isNew() {
        return createdAt == null;
    }
}

