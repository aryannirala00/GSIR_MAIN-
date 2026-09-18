package com.gsir.ai.planning;

public record ExecutionConstraints(
        int maximumSteps,
        long maximumRuntime,
        double maximumCost,
        int maximumToolCalls,
        int maximumRecursionDepth,
        long maximumMemory,
        int maximumExternalActions
) {
}

