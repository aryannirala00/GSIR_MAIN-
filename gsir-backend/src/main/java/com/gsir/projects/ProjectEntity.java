package com.gsir.projects;

import com.gsir.common.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity(name = "Project")
@Table(name = "projects")
public class ProjectEntity extends AuditedEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "execution_budget")
    private Double executionBudget;

    protected ProjectEntity() {}

    public ProjectEntity(UUID id, UUID workspaceId, String name, String description, String status) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.status = (status != null && !status.isBlank()) ? status : "ACTIVE";
    }

    public ProjectEntity(UUID id, UUID workspaceId, String name, String description) {
        this(id, workspaceId, name, description, "ACTIVE");
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() { return status; }

    public Double getExecutionBudget() { return executionBudget; }

    public void setExecutionBudget(Double executionBudget) { this.executionBudget = executionBudget; }
}


