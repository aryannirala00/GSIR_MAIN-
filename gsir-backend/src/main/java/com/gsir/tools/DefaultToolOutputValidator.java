package com.gsir.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.error.CommonErrors;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DefaultToolOutputValidator implements ToolOutputValidator {

    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory schemaFactory;
    private final ToolRegistry toolRegistry;
    private static final int MAX_OUTPUT_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    public DefaultToolOutputValidator(ObjectMapper objectMapper, ToolRegistry toolRegistry) {
        this.objectMapper = objectMapper;
        this.toolRegistry = toolRegistry;
        this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }

    @Override
    public void validate(String toolName, ToolResult result) {
        if (result == null || result.data() == null) {
            throw CommonErrors.invalid("Tool result cannot be null");
        }

        JsonNode outputNode = result.data();

        // 1. Size validation
        try {
            String jsonStr = objectMapper.writeValueAsString(outputNode);
            if (jsonStr.getBytes().length > MAX_OUTPUT_SIZE_BYTES) {
                throw CommonErrors.invalid("Tool output exceeds maximum allowed size of 10MB");
            }
        } catch (Exception e) {
            if (e instanceof com.gsir.common.error.GsirException) throw (RuntimeException) e;
            throw CommonErrors.internal("Failed to serialize output for size validation");
        }

        // 2. Security / Tenant Scope validation
        // Output must not contain arbitrary self-granted permissions or override the execution context
        // If the tool returns a tenant/project ID, it must match the execution context.
        if (outputNode.has("projectId") && !outputNode.get("projectId").isNull()) {
            String outputProjectId = outputNode.get("projectId").asText();
            if (result.context().projectId() != null && !result.context().projectId().toString().equals(outputProjectId)) {
                throw new com.gsir.common.error.GsirException("FORBIDDEN", "Tool output attempted to cross project boundaries", org.springframework.http.HttpStatus.FORBIDDEN);
            }
        }
        
        if (outputNode.has("workspaceId") && !outputNode.get("workspaceId").isNull()) {
            String outputWorkspaceId = outputNode.get("workspaceId").asText();
            if (result.context().workspaceId() != null && !result.context().workspaceId().toString().equals(outputWorkspaceId)) {
                throw new com.gsir.common.error.GsirException("FORBIDDEN", "Tool output attempted to cross workspace boundaries", org.springframework.http.HttpStatus.FORBIDDEN);
            }
        }

        if (outputNode.has("organizationId") && !outputNode.get("organizationId").isNull()) {
            String outputOrgId = outputNode.get("organizationId").asText();
            if (result.context().organizationId() != null && !result.context().organizationId().toString().equals(outputOrgId)) {
                throw new com.gsir.common.error.GsirException("FORBIDDEN", "Tool output attempted to cross organization boundaries", org.springframework.http.HttpStatus.FORBIDDEN);
            }
        }

        // Security limits (e.g. commands, paths)
        // If the tool output contains path traversal or command execution patterns in critical fields
        checkSecurityViolations(outputNode);

        // 3. Provenance validation
        // If the tool's output itself claims a provenance ID or execution ID, it must match the actual context.
        if (outputNode.has("executionId") && !outputNode.get("executionId").isNull()) {
            if (result.context().aiRunId() != null && !result.context().aiRunId().toString().equals(outputNode.get("executionId").asText())) {
                throw CommonErrors.invalid("Tool output provenance claim does not match execution context");
            }
        }

        // 4. Schema Validation
        Tool tool = toolRegistry.getTool(toolName, "1.0"); // TODO pass exact version if needed, or get from Tool
        String schemaString = tool.getDefinition().outputSchema();
        if (schemaString != null && !schemaString.isBlank()) {
            try {
                JsonNode schemaNode = objectMapper.readTree(schemaString);
                JsonSchema schema = schemaFactory.getSchema(schemaNode);
                Set<ValidationMessage> validationResult = schema.validate(outputNode);

                if (!validationResult.isEmpty()) {
                    StringBuilder errors = new StringBuilder();
                    validationResult.forEach(vm -> errors.append(vm.getMessage()).append("; "));
                    throw CommonErrors.invalid("Output schema validation failed: " + errors.toString());
                }
            } catch (Exception e) {
                if (e instanceof com.gsir.common.error.GsirException) throw (RuntimeException) e;
                throw CommonErrors.internal("Failed to validate output schema: " + e.getMessage());
            }
        }
    }

    private void checkSecurityViolations(JsonNode node) {
        if (node.isTextual()) {
            String text = node.asText().toLowerCase();
            // Basic heuristic to reject output that looks like it's trying to inject unauthorized paths or scripts
            // Real implementation would rely on strict schemas, but defense in depth here.
            if (text.contains("../") || text.contains("..\\")) {
                throw CommonErrors.invalid("Security violation: Path traversal patterns detected in output");
            }
        } else if (node.isObject()) {
            node.elements().forEachRemaining(this::checkSecurityViolations);
        } else if (node.isArray()) {
            node.elements().forEachRemaining(this::checkSecurityViolations);
        }
    }
}
