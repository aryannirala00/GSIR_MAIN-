package com.gsir.workflow;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "research_workflow_steps")
public class ResearchWorkflowStepEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @Column(name = "capability", nullable = false, length = 100)
    private String capability;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private WorkflowStepStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "inputs")
    private String inputs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "outputs")
    private String outputs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dependencies")
    private String dependencies; // JSON array of step IDs

    @Column(name = "execution_fingerprint", length = 255)
    private String executionFingerprint;

    @Column(name = "tool_version", length = 64)
    private String toolVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ResearchWorkflowStepEntity() {}

    public ResearchWorkflowStepEntity(UUID id, UUID workflowId, String stepName, String capability, String inputs, String dependencies) {
        this.id = id;
        this.workflowId = workflowId;
        this.stepName = stepName;
        this.capability = capability;
        this.status = WorkflowStepStatus.PENDING;
        this.inputs = inputs;
        this.dependencies = dependencies != null ? dependencies : "[]";
        this.outputs = null;
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
    public UUID getWorkflowId() { return workflowId; }
    public UUID getJobId() { return jobId; }
    public String getStepName() { return stepName; }
    public String getCapability() { return capability; }
    public WorkflowStepStatus getStatus() { return status; }
    public String getInputs() { return inputs; }
    public String getOutputs() { return outputs; }
    public String getDependencies() { return dependencies; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(WorkflowStepStatus status) { this.status = status; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public void setOutputs(String outputs) { this.outputs = outputs; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }

    public String getExecutionFingerprint() { return executionFingerprint; }
    public void setExecutionFingerprint(String executionFingerprint) { this.executionFingerprint = executionFingerprint; }

    public String getToolVersion() { return toolVersion; }
    public void setToolVersion(String toolVersion) { this.toolVersion = toolVersion; }

    @Override
    @Transient
    public boolean isNew() {
        return createdAt == null;
    }
}
