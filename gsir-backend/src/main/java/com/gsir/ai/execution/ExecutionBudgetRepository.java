package com.gsir.ai.execution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ExecutionBudgetRepository extends JpaRepository<ExecutionBudgetEntity, UUID> {

    @Modifying
    @Query("UPDATE ExecutionBudgetEntity e SET e.consumedSteps = e.consumedSteps + :amount WHERE e.executionId = :executionId AND e.consumedSteps + :amount <= e.maxSteps")
    int consumeStepsAtomic(@Param("executionId") UUID executionId, @Param("amount") int amount);

    @Modifying
    @Query("UPDATE ExecutionBudgetEntity e SET e.consumedToolCalls = e.consumedToolCalls + :amount WHERE e.executionId = :executionId AND e.consumedToolCalls + :amount <= e.maxToolCalls")
    int consumeToolCallsAtomic(@Param("executionId") UUID executionId, @Param("amount") int amount);

    @Modifying
    @Query("UPDATE ExecutionBudgetEntity e SET e.consumedDepth = e.consumedDepth + :amount WHERE e.executionId = :executionId AND e.consumedDepth + :amount <= e.maxDepth")
    int consumeDepthAtomic(@Param("executionId") UUID executionId, @Param("amount") int amount);

    @Modifying
    @Query("UPDATE ExecutionBudgetEntity e SET e.consumedTokens = e.consumedTokens + :amount WHERE e.executionId = :executionId AND e.consumedTokens + :amount <= e.maxTokens")
    int consumeTokensAtomic(@Param("executionId") UUID executionId, @Param("amount") long amount);

    @Modifying
    @Query("UPDATE ExecutionBudgetEntity e SET e.consumedCost = e.consumedCost + :amount WHERE e.executionId = :executionId AND e.consumedCost + :amount <= e.maxCost")
    int consumeCostAtomic(@Param("executionId") UUID executionId, @Param("amount") double amount);
}

