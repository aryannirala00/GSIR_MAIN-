package com.gsir.ai.safety;

import com.fasterxml.jackson.databind.JsonNode;
import com.gsir.tools.ToolDefinition;
import com.gsir.tools.ToolExecutionContext;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DefaultAgiSafetyBoundary implements AgiSafetyBoundary {
    
    private static final Set<String> SAFE_TOOLS = Set.of(
        "LITERATURE_SEARCH",
        "SUMMARIZE",
        "PROPOSE_SELF_IMPROVEMENT"
    );
    
    private static final Set<String> REVIEW_REQUIRED_TOOLS = Set.of(
        "EXPERIMENT_EXECUTION",
        "PHYSICAL_CONTROL",
        "DEPLOY",
        "HUMAN_APPROVAL"
    );

    @Override
    public SafetyEvaluationResult evaluateAction(ToolDefinition tool, JsonNode input, ToolExecutionContext context) {
        if (tool == null || tool.toolId() == null) {
            return new SafetyEvaluationResult(SafetyDecision.DENY, "Null tool or toolId", "POLICY-101-NULL");
        }
        
        String toolId = tool.toolId();
        
        if (SAFE_TOOLS.contains(toolId)) {
            return new SafetyEvaluationResult(SafetyDecision.ALLOW, "Tool explicitly classified as safe", "POLICY-101-SAFE");
        }
        
        if (REVIEW_REQUIRED_TOOLS.contains(toolId)) {
            return new SafetyEvaluationResult(SafetyDecision.REQUIRE_HUMAN_REVIEW, "Tool explicitly requires human review", "POLICY-101-HIL");
        }
        
        // Fail-closed for unknown actions
        return new SafetyEvaluationResult(SafetyDecision.DENY, "Tool capability not recognized in safety registry", "POLICY-101-UNKNOWN");
    }
}
