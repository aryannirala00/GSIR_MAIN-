package com.gsir.data.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dataset_shares")
public class DatasetShareEntity {
    @Id
    private UUID id;

    @Column(name = "dataset_id", nullable = false)
    private UUID datasetId;

    @Column(name = "shared_by", nullable = false)
    private UUID sharedBy;

    @Column(name = "shared_with", nullable = false)
    private UUID sharedWith;

    @Column(name = "permission_level", nullable = false)
    private String permissionLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDatasetId() { return datasetId; }
    public void setDatasetId(UUID datasetId) { this.datasetId = datasetId; }
    public UUID getSharedBy() { return sharedBy; }
    public void setSharedBy(UUID sharedBy) { this.sharedBy = sharedBy; }
    public UUID getSharedWith() { return sharedWith; }
    public void setSharedWith(UUID sharedWith) { this.sharedWith = sharedWith; }
    public String getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
