package com.gsir.execution.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "execution_costs")
public class ExecutionCostEntity {
    @Id private UUID id;
    @Column(name = "project_id", nullable = false) private UUID projectId;
    @Column(name = "workflow_id") private UUID workflowId;
    @Column(name = "step_id") private UUID stepId;
    @Column(name = "compute_cost", nullable = false) private Double computeCost;
    @Column(name = "model_cost", nullable = false) private Double modelCost;
    @Column(name = "api_cost", nullable = false) private Double apiCost;
    @Column(name = "total_cost", nullable = false) private Double totalCost;
    @Column(name = "currency", nullable = false) private String currency;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    public ExecutionCostEntity() {}

    public ExecutionCostEntity(UUID id, UUID projectId, UUID workflowId, UUID stepId, Double computeCost, Double modelCost, Double apiCost, String currency) {
        this.id = id;
        this.projectId = projectId;
        this.workflowId = workflowId;
        this.stepId = stepId;
        this.computeCost = computeCost != null ? computeCost : 0.0;
        this.modelCost = modelCost != null ? modelCost : 0.0;
        this.apiCost = apiCost != null ? apiCost : 0.0;
        this.totalCost = this.computeCost + this.modelCost + this.apiCost;
        this.currency = currency != null ? currency : "USD";
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public UUID getWorkflowId() { return workflowId; }
    public UUID getStepId() { return stepId; }
    public Double getComputeCost() { return computeCost; }
    public Double getModelCost() { return modelCost; }
    public Double getApiCost() { return apiCost; }
    public Double getTotalCost() { return totalCost; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
}
