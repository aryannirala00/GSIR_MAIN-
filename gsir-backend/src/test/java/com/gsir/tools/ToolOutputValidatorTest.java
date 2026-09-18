package com.gsir.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.error.GsirException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ToolOutputValidatorTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private DefaultToolOutputValidator validator;
    private ToolRegistry toolRegistry;
    private Tool mockTool;

    @BeforeEach
    void setUp() {
        mockTool = mock(Tool.class);
        ToolDefinition mockDef = new ToolDefinition("TEST_TOOL", "1.0", "desc", "cat", "{}", "{}", false, 30, true);
        when(mockTool.getDefinition()).thenReturn(mockDef);
        
        toolRegistry = new ToolRegistry(List.of(mockTool));
        validator = new DefaultToolOutputValidator(objectMapper, toolRegistry);
    }

    private ToolResult createResult(String jsonStr) throws Exception {
        return createResult(jsonStr, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }
    
    private ToolResult createResult(String jsonStr, UUID pId, UUID wId, UUID oId, UUID runId) throws Exception {
        JsonNode node = objectMapper.readTree(jsonStr);
        ToolExecutionContext ctx = new ToolExecutionContext(
                UUID.randomUUID(), oId, wId, pId, UUID.randomUUID(), runId
        );
        return new ToolResult(node, ctx, null);
    }

    @Test
    void testValidate_ValidOutput() throws Exception {
        ToolResult result = createResult("{\"status\":\"ok\"}");
        validator.validate("TEST_TOOL", result); // Should not throw
    }

    @Test
    void testValidate_SizeLimitExceeded() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"data\":\"");
        for (int i=0; i<11_000_000; i++) sb.append("A"); // >10MB
        sb.append("\"}");
        
        ToolResult result = createResult(sb.toString());
        assertThatThrownBy(() -> validator.validate("TEST_TOOL", result))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("exceeds maximum allowed size");
    }

    @Test
    void testValidate_SecurityPathTraversal() throws Exception {
        ToolResult result = createResult("{\"path\":\"../../../etc/passwd\"}");
        assertThatThrownBy(() -> validator.validate("TEST_TOOL", result))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("Security violation: Path traversal");
    }

    @Test
    void testValidate_TenantScopeViolation_Project() throws Exception {
        UUID validProjectId = UUID.randomUUID();
        UUID otherProjectId = UUID.randomUUID();
        
        // Output contains an unauthorized project ID
        ToolResult result = createResult("{\"projectId\":\"" + otherProjectId + "\"}", validProjectId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        
        assertThatThrownBy(() -> validator.validate("TEST_TOOL", result))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("cross project boundaries");
    }
    
    @Test
    void testValidate_TenantScopeViolation_Workspace() throws Exception {
        UUID validProjectId = UUID.randomUUID();
        UUID validWorkspaceId = UUID.randomUUID();
        UUID otherWorkspaceId = UUID.randomUUID();
        
        ToolResult result = createResult("{\"workspaceId\":\"" + otherWorkspaceId + "\"}", validProjectId, validWorkspaceId, UUID.randomUUID(), UUID.randomUUID());
        
        assertThatThrownBy(() -> validator.validate("TEST_TOOL", result))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("cross workspace boundaries");
    }

    @Test
    void testValidate_ProvenanceMismatch() throws Exception {
        UUID aiRunId = UUID.randomUUID();
        UUID fakeRunId = UUID.randomUUID();
        
        ToolResult result = createResult("{\"executionId\":\"" + fakeRunId + "\"}", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), aiRunId);
        
        assertThatThrownBy(() -> validator.validate("TEST_TOOL", result))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("provenance claim does not match execution context");
    }

    @Test
    void testValidate_NullResult() {
        assertThatThrownBy(() -> validator.validate("TEST_TOOL", null))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("Tool result cannot be null");
    }
    
    @Test
    void testValidate_SchemaViolation() throws Exception {
        String schema = "{ \"type\": \"object\", \"properties\": { \"title\": { \"type\": \"string\" } }, \"required\": [\"title\"] }";
        ToolDefinition schemaDef = new ToolDefinition("TEST_TOOL", "1.0", "desc", "cat", "{}", schema, false, 30, true);
        when(mockTool.getDefinition()).thenReturn(schemaDef);
        
        // Missing "title"
        ToolResult result = createResult("{\"other\":\"value\"}");
        
        assertThatThrownBy(() -> validator.validate("TEST_TOOL", result))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("Output schema validation failed");
    }
}
