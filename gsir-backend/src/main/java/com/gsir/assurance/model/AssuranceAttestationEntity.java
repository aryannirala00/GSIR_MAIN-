package com.gsir.assurance.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "assurance_attestations")
public class AssuranceAttestationEntity {

    @Id
    private UUID id;

    @Column(name = "artifact_id", nullable = false)
    private UUID artifactId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "assurance_level", nullable = false)
    private String assuranceLevel;

    @Column(name = "score", nullable = false)
    private Double score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attestation_metadata", columnDefinition = "jsonb")
    private Map<String, Object> attestationMetadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AssuranceAttestationEntity() {}

    public AssuranceAttestationEntity(UUID id, UUID artifactId, UUID projectId, String assuranceLevel, Double score, Map<String, Object> attestationMetadata) {
        this.id = id;
        this.artifactId = artifactId;
        this.projectId = projectId;
        this.assuranceLevel = assuranceLevel;
        this.score = score;
        this.attestationMetadata = attestationMetadata;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getArtifactId() { return artifactId; }
    public UUID getProjectId() { return projectId; }
    public String getAssuranceLevel() { return assuranceLevel; }
    public Double getScore() { return score; }
    public Map<String, Object> getAttestationMetadata() { return attestationMetadata; }
    public Instant getCreatedAt() { return createdAt; }
}
