package com.gsir.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.error.CommonErrors;
import com.gsir.projects.ProjectAccessService;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import jakarta.annotation.PreDestroy;

@Service
public class ToolExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionService.class);
    
    private final ToolRegistry toolRegistry;
    private final ProjectAccessService projectAccessService;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final ToolOutputValidator toolOutputValidator;
    private final JsonSchemaFactory schemaFactory;
    private final com.gsir.ai.safety.AgiSafetyBoundary safetyBoundary;
    private final com.gsir.ai.execution.ExecutionBudgetService budgetService;

    public ToolExecutionService(
            ToolRegistry toolRegistry,
            ProjectAccessService projectAccessService,
            ObjectMapper objectMapper,
            ToolOutputValidator toolOutputValidator,
            com.gsir.ai.safety.AgiSafetyBoundary safetyBoundary,
            com.gsir.ai.execution.ExecutionBudgetService budgetService) {
        this.toolRegistry = toolRegistry;
        this.projectAccessService = projectAccessService;
        this.objectMapper = objectMapper;
        this.toolOutputValidator = toolOutputValidator;
        this.safetyBoundary = safetyBoundary;
        this.budgetService = budgetService;
        this.executorService = Executors.newCachedThreadPool();
        this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public String executeTool(String capability, String version, String rawInputJson, ToolExecutionContext context) {
        // 1. Locate tool
        Tool tool = toolRegistry.getTool(capability, version);
        
        // 2. Authorize using context
        if (context.projectId() != null) {
            projectAccessService.requireAccess(context.projectId(), context.userId());
        }
        
        // 3. Validate input
        JsonNode inputNode;
        try {
            inputNode = objectMapper.readTree(rawInputJson);
        } catch (Exception e) {
            throw CommonErrors.invalid("Malformed JSON input for tool: " + capability);
        }

        validateSchema(tool.getDefinition().inputSchema(), inputNode, "Input");

        // 3.5. AGI Safety Boundary
        com.gsir.ai.safety.SafetyEvaluationResult safetyResult = safetyBoundary.evaluateAction(tool.getDefinition(), inputNode, context);
        if (safetyResult.decision() == com.gsir.ai.safety.SafetyDecision.DENY) {
            throw new com.gsir.ai.safety.SafetyViolationException("Action denied by safety boundary: " + safetyResult.reason());
        }
        if (safetyResult.decision() == com.gsir.ai.safety.SafetyDecision.REQUIRE_HUMAN_REVIEW && !context.humanApproved()) {
            throw new com.gsir.ai.safety.RequireHumanReviewException("Action requires human review: " + safetyResult.reason());
        }

        // 3.8. Budget Admission
        if (context.aiRunId() != null) {
            try {
                budgetService.consumeToolCall(context.aiRunId());
            } catch (IllegalStateException e) {
                throw new RuntimeException("Budget Exhausted: " + e.getMessage());
            }
        }

        // 4. Execute with Timeout Bounds
        Callable<JsonNode> task = () -> tool.execute(inputNode, context);
        Future<JsonNode> future = executorService.submit(task);
        
        try {
            int timeout = tool.getDefinition().timeoutSeconds();
            if (timeout <= 0) timeout = 30; // default 30s
            
            JsonNode outputNode = future.get(timeout, TimeUnit.SECONDS);
            
            // 5. Validate Output (Part 93)
            // Create the ToolResult for the validator boundary
            ToolResult result = new ToolResult(outputNode, context, null);
            toolOutputValidator.validate(capability, result);

            return objectMapper.writeValueAsString(outputNode);
            
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("Tool {} timed out after {} seconds", capability, tool.getDefinition().timeoutSeconds());
            throw new RuntimeException("Tool execution timed out", e);
        } catch (Exception e) {
            log.error("Tool execution failed: {}", capability, e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    private void validateSchema(String schemaString, JsonNode dataNode, String phase) {
        if (schemaString == null || schemaString.isBlank()) {
            return;
        }
        try {
            JsonNode schemaNode = objectMapper.readTree(schemaString);
            JsonSchema schema = schemaFactory.getSchema(schemaNode);
            Set<ValidationMessage> validationResult = schema.validate(dataNode);

            if (!validationResult.isEmpty()) {
                StringBuilder errors = new StringBuilder();
                validationResult.forEach(vm -> errors.append(vm.getMessage()).append("; "));
                throw CommonErrors.invalid(phase + " schema validation failed: " + errors.toString());
            }
        } catch (Exception e) {
            if (e instanceof com.gsir.common.error.GsirException) {
                throw (RuntimeException) e;
            }
            throw CommonErrors.internal("Failed to validate " + phase.toLowerCase() + " schema: " + e.getMessage());
        }
    }
}
