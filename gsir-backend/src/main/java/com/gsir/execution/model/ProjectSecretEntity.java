package com.gsir.execution.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_secrets")
public class ProjectSecretEntity {
    @Id private UUID id;
    @Column(name = "project_id", nullable = false) private UUID projectId;
    @Column(name = "secret_name", nullable = false) private String secretName;
    @Column(name = "encrypted_value", nullable = false) private String encryptedValue;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public ProjectSecretEntity() {}

    public ProjectSecretEntity(UUID id, UUID projectId, String secretName, String encryptedValue) {
        this.id = id;
        this.projectId = projectId;
        this.secretName = secretName;
        this.encryptedValue = encryptedValue;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    
    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public String getSecretName() { return secretName; }
    public String getEncryptedValue() { return encryptedValue; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
