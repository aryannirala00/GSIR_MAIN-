package com.gsir.ai.execution;

import com.gsir.ai.planning.Plan;
import java.util.Map;
import java.util.UUID;

public record ExecutionState(
    UUID executionId,
    String objective,
    Plan plan,
    UUID currentStepId,
    Map<String, Object> evidence,
    Map<String, Object> toolResults,
    int remainingBudget,
    String status,
    UUID projectId,
    UUID userId
) {
    public ExecutionState {
        evidence = evidence == null ? Map.of() : Map.copyOf(evidence);
        toolResults = toolResults == null ? Map.of() : Map.copyOf(toolResults);
    }
}
