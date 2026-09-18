package com.gsir.ai.execution;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "execution_budgets")
public class ExecutionBudgetEntity {

    @Id
    @Column(name = "execution_id")
    private UUID executionId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "max_steps", nullable = false)
    private int maxSteps;

    @Column(name = "max_tool_calls", nullable = false)
    private int maxToolCalls;

    @Column(name = "max_depth", nullable = false)
    private int maxDepth;

    @Column(name = "max_runtime_millis", nullable = false)
    private long maxRuntimeMillis;

    @Column(name = "max_tokens", nullable = false)
    private long maxTokens;

    @Column(name = "max_cost", nullable = false)
    private double maxCost;

    @Column(name = "consumed_steps", nullable = false)
    private int consumedSteps = 0;

    @Column(name = "consumed_tool_calls", nullable = false)
    private int consumedToolCalls = 0;

    @Column(name = "consumed_depth", nullable = false)
    private int consumedDepth = 0;

    @Column(name = "consumed_tokens", nullable = false)
    private long consumedTokens = 0;

    @Column(name = "consumed_cost", nullable = false)
    private double consumedCost = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ExecutionBudgetEntity() {}

    public ExecutionBudgetEntity(UUID executionId, UUID projectId, int maxSteps, int maxToolCalls, int maxDepth, long maxRuntimeMillis, long maxTokens, double maxCost) {
        this.executionId = executionId;
        this.projectId = projectId;
        this.maxSteps = maxSteps;
        this.maxToolCalls = maxToolCalls;
        this.maxDepth = maxDepth;
        this.maxRuntimeMillis = maxRuntimeMillis;
        this.maxTokens = maxTokens;
        this.maxCost = maxCost;
        this.createdAt = Instant.now();
    }
    
    // Test constructor
    public ExecutionBudgetEntity(UUID executionId, UUID projectId, int maxSteps, int maxToolCalls, int maxDepth, long maxRuntimeMillis, long maxTokens, double maxCost, Instant createdAt) {
        this.executionId = executionId;
        this.projectId = projectId;
        this.maxSteps = maxSteps;
        this.maxToolCalls = maxToolCalls;
        this.maxDepth = maxDepth;
        this.maxRuntimeMillis = maxRuntimeMillis;
        this.maxTokens = maxTokens;
        this.maxCost = maxCost;
        this.createdAt = createdAt;
    }

    public UUID getExecutionId() { return executionId; }
    public UUID getProjectId() { return projectId; }
    public int getMaxSteps() { return maxSteps; }
    public int getMaxToolCalls() { return maxToolCalls; }
    public int getMaxDepth() { return maxDepth; }
    public long getMaxRuntimeMillis() { return maxRuntimeMillis; }
    public long getMaxTokens() { return maxTokens; }
    public double getMaxCost() { return maxCost; }
    
    public int getConsumedSteps() { return consumedSteps; }
    public int getConsumedToolCalls() { return consumedToolCalls; }
    public int getConsumedDepth() { return consumedDepth; }
    public long getConsumedTokens() { return consumedTokens; }
    public double getConsumedCost() { return consumedCost; }
    public Instant getCreatedAt() { return createdAt; }
}

