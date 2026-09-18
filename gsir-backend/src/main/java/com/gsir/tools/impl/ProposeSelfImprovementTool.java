package com.gsir.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gsir.ai.improvement.SelfImprovementCategory;
import com.gsir.ai.improvement.SelfImprovementEvaluationResult;
import com.gsir.ai.improvement.SelfImprovementProposal;
import com.gsir.ai.improvement.SelfImprovementService;
import com.gsir.tools.Tool;
import com.gsir.tools.ToolDefinition;
import com.gsir.tools.ToolExecutionContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProposeSelfImprovementTool implements Tool {

    private final ToolDefinition definition;
    private final SelfImprovementService selfImprovementService;
    private final ObjectMapper objectMapper;

    public ProposeSelfImprovementTool(SelfImprovementService selfImprovementService, ObjectMapper objectMapper) {
        this.selfImprovementService = selfImprovementService;
        this.objectMapper = objectMapper;
        this.definition = new ToolDefinition(
                "PROPOSE_SELF_IMPROVEMENT",
                "1.0",
                "Proposes a recursive self-improvement candidate against a baseline.",
                "SELF_IMPROVEMENT",
                "{\"type\":\"object\",\"properties\":{\"category\":{\"type\":\"string\"},\"description\":{\"type\":\"string\"},\"baselineId\":{\"type\":\"string\"},\"candidatePayload\":{\"type\":\"string\"},\"expectedBenefit\":{\"type\":\"string\"},\"testPlan\":{\"type\":\"string\"},\"rollbackPlan\":{\"type\":\"string\"}}}",
                "{\"type\":\"object\",\"properties\":{\"approved\":{\"type\":\"boolean\"},\"requiresHumanReview\":{\"type\":\"boolean\"},\"regressionDetected\":{\"type\":\"boolean\"},\"reason\":{\"type\":\"string\"}}}",
                false,
                60,
                true
        );
    }

    @Override
    public ToolDefinition getDefinition() {
        return definition;
    }

    @Override
    public JsonNode execute(JsonNode input, ToolExecutionContext context) {
        try {
            SelfImprovementCategory category = SelfImprovementCategory.valueOf(input.get("category").asText());
            String description = input.has("description") ? input.get("description").asText() : "";
            String baselineId = input.has("baselineId") ? input.get("baselineId").asText() : "";
            String candidatePayload = input.has("candidatePayload") ? input.get("candidatePayload").asText() : "";
            String expectedBenefit = input.has("expectedBenefit") ? input.get("expectedBenefit").asText() : "";
            String testPlan = input.has("testPlan") ? input.get("testPlan").asText() : "";
            String rollbackPlan = input.has("rollbackPlan") ? input.get("rollbackPlan").asText() : "";

            SelfImprovementProposal proposal = new SelfImprovementProposal(
                UUID.randomUUID(),
                context.projectId(),
                category,
                description,
                baselineId,
                candidatePayload,
                expectedBenefit,
                testPlan,
                rollbackPlan
            );

            SelfImprovementEvaluationResult evalResult = selfImprovementService.proposeImprovement(
                proposal, context.userId(), context.aiRunId()
            );

            ObjectNode result = objectMapper.createObjectNode();
            result.put("approved", evalResult.approved());
            result.put("requiresHumanReview", evalResult.requiresHumanReview());
            result.put("regressionDetected", evalResult.regressionDetected());
            result.put("reason", evalResult.reason());
            return result;
        } catch (IllegalArgumentException e) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", "Invalid category. Must be one of SelfImprovementCategory values.");
            return error;
        }
    }
}
