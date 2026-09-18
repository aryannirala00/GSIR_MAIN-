package com.gsir.ai.safety;

import com.fasterxml.jackson.databind.JsonNode;
import com.gsir.tools.ToolDefinition;
import com.gsir.tools.ToolExecutionContext;

public interface AgiSafetyBoundary {
    SafetyEvaluationResult evaluateAction(ToolDefinition tool, JsonNode input, ToolExecutionContext context);
}
