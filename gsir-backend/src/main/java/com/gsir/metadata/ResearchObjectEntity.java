package com.gsir.metadata;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "research_objects")
public class ResearchObjectEntity {

    @Id
    private UUID id;
    
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "object_type", nullable = false)
    private String objectType;

    @Column(name = "persistent_identifier")
    private String persistentIdentifier;

    @Column(name = "version_id", nullable = false)
    private String versionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "schema_id")
    private String schemaId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "status", nullable = false)
    private String status;

    public ResearchObjectEntity() {}

    public ResearchObjectEntity(UUID id, UUID projectId, String objectType, String persistentIdentifier, String versionId, String metadata, String schemaId) {
        this.id = id;
        this.projectId = projectId;
        this.objectType = objectType;
        this.persistentIdentifier = persistentIdentifier;
        this.versionId = versionId;
        this.metadata = metadata;
        this.schemaId = schemaId;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.status = "ACTIVE";
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public String getObjectType() { return objectType; }
    public String getPersistentIdentifier() { return persistentIdentifier; }
    public String getVersionId() { return versionId; }
    public String getMetadata() { return metadata; }
    public String getSchemaId() { return schemaId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getStatus() { return status; }
    
    public void setMetadata(String metadata) { this.metadata = metadata; this.updatedAt = Instant.now(); }
    public void setVersionId(String versionId) { this.versionId = versionId; this.updatedAt = Instant.now(); }
    public void setStatus(String status) { this.status = status; this.updatedAt = Instant.now(); }
}
