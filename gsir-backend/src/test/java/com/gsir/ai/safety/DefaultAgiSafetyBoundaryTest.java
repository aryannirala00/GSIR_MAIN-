package com.gsir.ai.safety;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.tools.ToolDefinition;
import com.gsir.tools.ToolExecutionContext;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DefaultAgiSafetyBoundaryTest {
    
    private DefaultAgiSafetyBoundary boundary = new DefaultAgiSafetyBoundary();
    private ObjectMapper mapper = new ObjectMapper();
    private ToolExecutionContext context = new ToolExecutionContext(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), false, null);

    @Test
    void testSourceCodeModifyDenied() {
        ToolDefinition tool = new ToolDefinition("SOURCE_CODE_MODIFY", "1.0", "desc", "cat", "{}", "{}", false, 30, true);
        SafetyEvaluationResult result = boundary.evaluateAction(tool, mapper.createObjectNode(), context);
        assertEquals(SafetyDecision.DENY, result.decision());
        assertTrue(result.reason().contains("not recognized"));
    }

    @Test
    void testUnknownToolDeniedByDefault() {
        ToolDefinition tool = new ToolDefinition("RANDOM_HACK_TOOL", "1.0", "desc", "cat", "{}", "{}", false, 30, true);
        SafetyEvaluationResult result = boundary.evaluateAction(tool, mapper.createObjectNode(), context);
        assertEquals(SafetyDecision.DENY, result.decision());
    }

    @Test
    void testExperimentRequiresHumanReview() {
        ToolDefinition tool = new ToolDefinition("EXPERIMENT_EXECUTION", "1.0", "desc", "cat", "{}", "{}", false, 30, true);
        SafetyEvaluationResult result = boundary.evaluateAction(tool, mapper.createObjectNode(), context);
        assertEquals(SafetyDecision.REQUIRE_HUMAN_REVIEW, result.decision());
    }

    @Test
    void testNormalToolAllowed() {
        ToolDefinition tool = new ToolDefinition("LITERATURE_SEARCH", "1.0", "desc", "cat", "{}", "{}", false, 30, true);
        SafetyEvaluationResult result = boundary.evaluateAction(tool, mapper.createObjectNode(), context);
        assertEquals(SafetyDecision.ALLOW, result.decision());
    }
}
