package com.gsir.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.gsir.common.error.CommonErrors;
import com.gsir.projects.ProjectAccessService;
import com.gsir.projects.ProjectEntity;
import com.gsir.tools.impl.LiteratureSearchTool;
import com.gsir.tools.impl.SummarizeTool;
import com.gsir.tools.provider.ResearchSourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ToolExecutionServiceTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private ToolExecutionService service;
    private ToolRegistry registry;
    private boolean shouldThrowAuth = false;

    @BeforeEach
    void setUp() {
        ResearchSourceProvider mockProvider = new ResearchSourceProvider() {
            @Override
            public String getProviderName() {
                return "OpenAlex";
            }

            @Override
            public JsonNode search(String query, int limit) {
                ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
                arrayNode.add(JsonNodeFactory.instance.objectNode().put("title", "Quantum Computing"));
                return arrayNode;
            }
        };

        registry = new ToolRegistry(List.of(new LiteratureSearchTool(List.of(mockProvider)), new SummarizeTool()));
        
        ProjectAccessService stubService = new ProjectAccessService(null) {
            @Override
            public ProjectEntity requireAccess(UUID projectId, UUID userId) {
                if (shouldThrowAuth) {
                    throw CommonErrors.forbidden();
                }
                return new ProjectEntity(projectId, UUID.randomUUID(), "Test", "Desc");
            }
        };

        ToolOutputValidator stubValidator = new DefaultToolOutputValidator(objectMapper, registry);

        com.gsir.ai.safety.AgiSafetyBoundary mockBoundary = mock(com.gsir.ai.safety.AgiSafetyBoundary.class);
        when(mockBoundary.evaluateAction(any(), any(), any())).thenReturn(
            new com.gsir.ai.safety.SafetyEvaluationResult(com.gsir.ai.safety.SafetyDecision.ALLOW, "OK", "TEST")
        );

        com.gsir.ai.execution.ExecutionBudgetService fakeBudgetService = new com.gsir.ai.execution.ExecutionBudgetService(null, null, null) {
            @Override public void createBudget(java.util.UUID e, java.util.UUID p, com.gsir.ai.execution.ExecutionBudget b) {}
            @Override public void consumeStep(java.util.UUID e) {}
            @Override public void consumeToolCall(java.util.UUID e) {}
            @Override public void consumeTokens(java.util.UUID e, long t) {}
            @Override public void consumeCost(java.util.UUID e, double c) {}
            @Override public void validateGuard(java.util.UUID e) {}
        };
        service = new ToolExecutionService(registry, stubService, objectMapper, stubValidator, mockBoundary, fakeBudgetService);
    }

    private ToolExecutionContext createTestContext(UUID projectId, UUID userId) {
        return new ToolExecutionContext(
                userId,
                UUID.randomUUID(), // organizationId
                UUID.randomUUID(), // workspaceId
                projectId,
                UUID.randomUUID(), // requestId
                UUID.randomUUID()  // aiRunId
        );
    }

    @Test
    void testExecuteTool_Success() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        shouldThrowAuth = false;

        ToolExecutionContext context = createTestContext(projectId, userId);
        String result = service.executeTool("LITERATURE_SEARCH", "1.0", "{\"query\":\"quantum\"}", context);
        
        assertThat(result).contains("\"results\"");
        assertThat(result).contains("\"query_executed\":\"quantum\"");
        assertThat(result).contains("Quantum Computing");
    }

    @Test
    void testExecuteTool_UnknownTool() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ToolExecutionContext context = createTestContext(projectId, userId);

        assertThatThrownBy(() -> service.executeTool("UNKNOWN", "1.0", "{\"query\":\"quantum\"}", context))
            .isInstanceOf(com.gsir.common.error.GsirException.class)
            .hasMessageContaining("Tool not found");
    }

    @Test
    void testExecuteTool_Unauthorized() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        shouldThrowAuth = true;
        ToolExecutionContext context = createTestContext(projectId, userId);

        assertThatThrownBy(() -> service.executeTool("LITERATURE_SEARCH", "1.0", "{\"query\":\"quantum\"}", context))
            .isInstanceOf(com.gsir.common.error.GsirException.class)
            .hasMessageContaining("You do not have permission");
    }

    @Test
    void testExecuteTool_MalformedInput() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        shouldThrowAuth = false;
        ToolExecutionContext context = createTestContext(projectId, userId);

        assertThatThrownBy(() -> service.executeTool("LITERATURE_SEARCH", "1.0", "NOT_JSON", context))
            .isInstanceOf(com.gsir.common.error.GsirException.class)
            .hasMessageContaining("Malformed JSON");
    }
    
    @Test
    void testExecuteTool_InvalidSchema() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        shouldThrowAuth = false;
        ToolExecutionContext context = createTestContext(projectId, userId);

        assertThatThrownBy(() -> service.executeTool("LITERATURE_SEARCH", "1.0", "{\"other\":\"quantum\"}", context))
            .isInstanceOf(com.gsir.common.error.GsirException.class)
            .hasMessageContaining("Input schema validation failed");
    }

    @Test
    void testContextCreationValidation() {
        assertThatThrownBy(() -> {
            new ToolExecutionContext(null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        }).isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void testExecuteTool_MissingProjectId_AllowedIfToolSupportsIt() {
        // ToolExecutionService allows missing projectId if tool doesn't explicitly need tenant auth
        // But the context must correctly reflect null
        UUID userId = UUID.randomUUID();
        ToolExecutionContext context = new ToolExecutionContext(
                userId, UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        );
        shouldThrowAuth = false;
        
        String result = service.executeTool("LITERATURE_SEARCH", "1.0", "{\"query\":\"test\"}", context);
        assertThat(result).contains("query_executed");
    }
    
    @Test
    void testExecuteTool_ConcurrentIndependentContexts() throws Exception {
        UUID projectId1 = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        ToolExecutionContext context1 = createTestContext(projectId1, userId1);

        UUID projectId2 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        ToolExecutionContext context2 = createTestContext(projectId2, userId2);

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        
        java.util.concurrent.Future<String> future1 = executor.submit(() -> service.executeTool("LITERATURE_SEARCH", "1.0", "{\"query\":\"q1\"}", context1));
        java.util.concurrent.Future<String> future2 = executor.submit(() -> service.executeTool("LITERATURE_SEARCH", "1.0", "{\"query\":\"q2\"}", context2));
        
        String res1 = future1.get();
        String res2 = future2.get();
        
        assertThat(res1).contains("q1");
        assertThat(res2).contains("q2");
        executor.shutdown();
    }
    @Test
    public void testSafetyOrdering_DenyBeforeBudget() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ToolExecutionContext context = createTestContext(projectId, userId);

        com.gsir.ai.safety.AgiSafetyBoundary mockBoundary = mock(com.gsir.ai.safety.AgiSafetyBoundary.class);
        when(mockBoundary.evaluateAction(any(), any(), any())).thenReturn(
            new com.gsir.ai.safety.SafetyEvaluationResult(com.gsir.ai.safety.SafetyDecision.DENY, "DENIED", "TEST")
        );

        com.gsir.ai.execution.ExecutionBudgetService mockBudgetService = mock(com.gsir.ai.execution.ExecutionBudgetService.class);
        
        ToolExecutionService localService = new ToolExecutionService(registry, mock(ProjectAccessService.class), objectMapper, mock(ToolOutputValidator.class), mockBoundary, mockBudgetService);

        assertThatThrownBy(() -> localService.executeTool("LITERATURE_SEARCH", "1.0", "{\"query\":\"test\"}", context))
            .isInstanceOf(com.gsir.ai.safety.SafetyViolationException.class)
            .hasMessageContaining("Action denied by safety boundary");

        // Verify budget was never consumed
        verify(mockBudgetService, never()).consumeToolCall(any());
    }

    @Test
    public void testSafetyOrdering_RequireHumanReviewBeforeBudget() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ToolExecutionContext context = createTestContext(projectId, userId);

        com.gsir.ai.safety.AgiSafetyBoundary mockBoundary = mock(com.gsir.ai.safety.AgiSafetyBoundary.class);
        when(mockBoundary.evaluateAction(any(), any(), any())).thenReturn(
            new com.gsir.ai.safety.SafetyEvaluationResult(com.gsir.ai.safety.SafetyDecision.REQUIRE_HUMAN_REVIEW, "REVIEW", "TEST")
        );

        com.gsir.ai.execution.ExecutionBudgetService mockBudgetService = mock(com.gsir.ai.execution.ExecutionBudgetService.class);
        
        ToolExecutionService localService = new ToolExecutionService(registry, mock(ProjectAccessService.class), objectMapper, mock(ToolOutputValidator.class), mockBoundary, mockBudgetService);

        assertThatThrownBy(() -> localService.executeTool("LITERATURE_SEARCH", "1.0", "{\"query\":\"test\"}", context))
            .isInstanceOf(com.gsir.ai.safety.RequireHumanReviewException.class)
            .hasMessageContaining("Action requires human review");

        // Verify budget was never consumed
        verify(mockBudgetService, never()).consumeToolCall(any());
    }

    @Test
    public void testSafetyOrdering_AllowWithExhaustedBudget() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ToolExecutionContext context = createTestContext(projectId, userId);

        com.gsir.ai.safety.AgiSafetyBoundary mockBoundary = mock(com.gsir.ai.safety.AgiSafetyBoundary.class);
        when(mockBoundary.evaluateAction(any(), any(), any())).thenReturn(
            new com.gsir.ai.safety.SafetyEvaluationResult(com.gsir.ai.safety.SafetyDecision.ALLOW, "OK", "TEST")
        );

        com.gsir.ai.execution.ExecutionBudgetService mockBudgetService = mock(com.gsir.ai.execution.ExecutionBudgetService.class);
        doThrow(new IllegalStateException("Budget exhausted")).when(mockBudgetService).consumeToolCall(any());
        
        ToolExecutionService localService = new ToolExecutionService(registry, mock(ProjectAccessService.class), objectMapper, mock(ToolOutputValidator.class), mockBoundary, mockBudgetService);

        assertThatThrownBy(() -> localService.executeTool("LITERATURE_SEARCH", "1.0", "{\"query\":\"test\"}", context))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Budget Exhausted");

        // Verify safety boundary was called
        verify(mockBoundary, times(1)).evaluateAction(any(), any(), any());
    }
}
