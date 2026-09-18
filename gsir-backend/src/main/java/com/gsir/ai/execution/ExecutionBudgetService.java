package com.gsir.ai.execution;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExecutionBudgetService {

    private final ExecutionBudgetRepository repository;
    private final java.time.Clock clock;
    private final com.gsir.provenance.service.ExecutionProvenanceService provenanceService;

    public ExecutionBudgetService(ExecutionBudgetRepository repository, org.springframework.beans.factory.ObjectProvider<java.time.Clock> clockProvider, org.springframework.beans.factory.ObjectProvider<com.gsir.provenance.service.ExecutionProvenanceService> provenanceProvider) {
        this.repository = repository;
        this.clock = clockProvider.getIfAvailable(() -> java.time.Clock.systemUTC());
        this.provenanceService = provenanceProvider.getIfAvailable();
    }

    @Transactional
    public void createBudget(UUID executionId, UUID projectId, ExecutionBudget budget) {
        ExecutionBudgetEntity entity = new ExecutionBudgetEntity(
                executionId, projectId,
                budget.maxSteps(), budget.maxToolCalls(), budget.maxDepth(),
                budget.maxRuntimeMillis(), budget.maxTokens(), budget.maxCost(),
                java.time.Instant.now(clock)
        );
        repository.save(entity);
        if (provenanceService != null) {
            provenanceService.recordBudgetEvent(executionId, projectId, "CREATED", "Budget created");
        }
    }

    @Transactional
    public void consumeStep(UUID executionId) {
        int updated = repository.consumeStepsAtomic(executionId, 1);
        if (updated == 0) {
            if (provenanceService != null) provenanceService.recordBudgetEvent(executionId, null, "EXHAUSTED", "maxSteps exceeded");
            throw new IllegalStateException("Budget exhausted: maxSteps exceeded");
        }
    }

    @Transactional
    public void consumeToolCall(UUID executionId) {
        int updated = repository.consumeToolCallsAtomic(executionId, 1);
        if (updated == 0) {
            if (provenanceService != null) provenanceService.recordBudgetEvent(executionId, null, "EXHAUSTED", "maxToolCalls exceeded");
            throw new IllegalStateException("Budget exhausted: maxToolCalls exceeded");
        }
    }

    @Transactional
    public void consumeDepth(UUID executionId, int depth) {
        int updated = repository.consumeDepthAtomic(executionId, depth);
        if (updated == 0) {
            if (provenanceService != null) provenanceService.recordBudgetEvent(executionId, null, "EXHAUSTED", "maxDepth exceeded");
            throw new IllegalStateException("Budget exhausted: maxDepth exceeded");
        }
    }

    @Transactional
    public void consumeTokens(UUID executionId, long tokens) {
        int updated = repository.consumeTokensAtomic(executionId, tokens);
        if (updated == 0) {
            if (provenanceService != null) provenanceService.recordBudgetEvent(executionId, null, "EXHAUSTED", "maxTokens exceeded");
            throw new IllegalStateException("Budget exhausted: maxTokens exceeded");
        }
    }

    @Transactional
    public void consumeCost(UUID executionId, double cost) {
        int updated = repository.consumeCostAtomic(executionId, cost);
        if (updated == 0) {
            if (provenanceService != null) provenanceService.recordBudgetEvent(executionId, null, "EXHAUSTED", "maxCost exceeded");
            throw new IllegalStateException("Budget exhausted: maxCost exceeded");
        }
    }

    @Transactional(readOnly = true)
    public void validateGuard(UUID executionId) {
        ExecutionBudgetEntity entity = repository.findById(executionId)
                .orElseThrow(() -> new IllegalStateException("Budget unavailable"));
                
        // Local evaluation guard in case limits were exceeded by non-atomic means or time limits
        long runtime = clock.millis() - entity.getCreatedAt().toEpochMilli();
        try {
            BudgetGuard.requireWithin(
                    new ExecutionBudget(entity.getMaxSteps(), entity.getMaxToolCalls(), entity.getMaxDepth(), entity.getMaxRuntimeMillis(), entity.getMaxTokens(), entity.getMaxCost()),
                    entity.getConsumedSteps(), entity.getConsumedToolCalls(), entity.getConsumedDepth(),
                    runtime, entity.getConsumedTokens(), entity.getConsumedCost()
            );
        } catch (IllegalStateException e) {
            if (provenanceService != null) provenanceService.recordBudgetEvent(executionId, entity.getProjectId(), "EXHAUSTED", "validateGuard failed: " + e.getMessage());
            throw e;
        }
    }
}

