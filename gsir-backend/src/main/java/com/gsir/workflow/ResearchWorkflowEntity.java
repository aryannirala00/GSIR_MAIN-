package com.gsir.workflow;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "research_workflows")
public class ResearchWorkflowEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "objective", nullable = false)
    private String objective;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "research_state")
    private String researchState;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private WorkflowStatus status;

    @Column(name = "reproducibility_fingerprint", length = 255)
    private String reproducibilityFingerprint;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "environment_metadata")
    private String environmentMetadata;

    @Column(name = "reproducibility_status", length = 64)
    private String reproducibilityStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ResearchWorkflowEntity() {}

    public ResearchWorkflowEntity(UUID id, UUID projectId, UUID userId, String objective) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.objective = objective;
        this.status = WorkflowStatus.CREATED;
        this.researchState = "{}";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public UUID getUserId() { return userId; }
    public String getObjective() { return objective; }
    public String getResearchState() { return researchState; }
    public WorkflowStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(WorkflowStatus status) { this.status = status; }
    public void setResearchState(String researchState) { this.researchState = researchState; }

    public String getReproducibilityFingerprint() { return reproducibilityFingerprint; }
    public void setReproducibilityFingerprint(String reproducibilityFingerprint) { this.reproducibilityFingerprint = reproducibilityFingerprint; }

    public String getEnvironmentMetadata() { return environmentMetadata; }
    public void setEnvironmentMetadata(String environmentMetadata) { this.environmentMetadata = environmentMetadata; }

    public String getReproducibilityStatus() { return reproducibilityStatus; }
    public void setReproducibilityStatus(String reproducibilityStatus) { this.reproducibilityStatus = reproducibilityStatus; }

    @Override
    @Transient
    public boolean isNew() {
        return createdAt == null;
    }
}
