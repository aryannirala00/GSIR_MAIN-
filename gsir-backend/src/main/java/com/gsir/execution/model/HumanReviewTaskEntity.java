package com.gsir.execution.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;

@Entity
@Table(name = "human_review_tasks")
public class HumanReviewTaskEntity {
    @Id private UUID id;
    @Column(name = "project_id", nullable = false) private UUID projectId;
    @Column(name = "workflow_id", nullable = false) private UUID workflowId;
    @Column(name = "step_id", nullable = false) private UUID stepId;
    @Column(name = "reviewer_id") private UUID reviewerId;
    @Column(name = "status", nullable = false) private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "review_context", columnDefinition = "jsonb")
    private Map<String, Object> reviewContext;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "decision", columnDefinition = "jsonb")
    private Map<String, Object> decision;

    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "resolved_at") private Instant resolvedAt;

    public HumanReviewTaskEntity() {}

    public HumanReviewTaskEntity(UUID id, UUID projectId, UUID workflowId, UUID stepId, Map<String, Object> reviewContext) {
        this.id = id;
        this.projectId = projectId;
        this.workflowId = workflowId;
        this.stepId = stepId;
        this.status = "PENDING";
        this.reviewContext = reviewContext;
        this.createdAt = Instant.now();
    }
    
    public void resolve(UUID reviewerId, Map<String, Object> decision) {
        this.reviewerId = reviewerId;
        this.decision = decision;
        this.status = "RESOLVED";
        this.resolvedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public UUID getWorkflowId() { return workflowId; }
    public UUID getStepId() { return stepId; }
    public UUID getReviewerId() { return reviewerId; }
    public String getStatus() { return status; }
    public Map<String, Object> getReviewContext() { return reviewContext; }
    public Map<String, Object> getDecision() { return decision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
}
