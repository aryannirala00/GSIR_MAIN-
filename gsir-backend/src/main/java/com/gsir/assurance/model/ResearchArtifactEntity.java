package com.gsir.assurance.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "research_artifacts")
public class ResearchArtifactEntity {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "object_type", nullable = false)
    private String objectType;

    @Column(name = "location_uri", nullable = false)
    private String locationUri;

    @Column(name = "expected_hash", nullable = false)
    private String expectedHash;

    @Column(name = "hash_algorithm", nullable = false)
    private String hashAlgorithm;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ResearchArtifactEntity() {}

    public ResearchArtifactEntity(UUID id, UUID projectId, String objectType, String locationUri, String expectedHash, String hashAlgorithm) {
        this.id = id;
        this.projectId = projectId;
        this.objectType = objectType;
        this.locationUri = locationUri;
        this.expectedHash = expectedHash;
        this.hashAlgorithm = hashAlgorithm;
        this.status = "REGISTERED";
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public String getObjectType() { return objectType; }
    public String getLocationUri() { return locationUri; }
    public String getExpectedHash() { return expectedHash; }
    public String getHashAlgorithm() { return hashAlgorithm; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    
    public void setStatus(String status) { this.status = status; }
}

