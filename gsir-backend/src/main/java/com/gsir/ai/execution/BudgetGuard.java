package com.gsir.ai.execution;

public final class BudgetGuard {
    private BudgetGuard() {}

    public static void requireWithin(
            ExecutionBudget budget,
            int steps,
            int toolCalls,
            int depth,
            long runtime,
            long tokens,
            double cost) {
        if (steps > budget.maxSteps()
                || toolCalls > budget.maxToolCalls()
                || depth > budget.maxDepth()
                || runtime > budget.maxRuntimeMillis()
                || tokens > budget.maxTokens()
                || cost > budget.maxCost()) {
            throw new IllegalStateException("Budget exhausted");
        }
    }
}

