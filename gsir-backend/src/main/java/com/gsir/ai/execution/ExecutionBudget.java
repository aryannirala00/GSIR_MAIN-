package com.gsir.ai.execution;

public record ExecutionBudget(
        int maxSteps,
        int maxToolCalls,
        int maxDepth,
        long maxRuntimeMillis,
        long maxTokens,
        double maxCost) {
}

