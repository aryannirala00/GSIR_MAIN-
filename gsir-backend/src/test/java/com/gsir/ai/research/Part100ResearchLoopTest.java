package com.gsir.ai.research;

import com.gsir.ai.execution.AgentExecutor;
import com.gsir.ai.execution.ExecutionState;
import com.gsir.ai.metacognition.MetacognitionService;
import com.gsir.ai.planning.Plan;
import com.gsir.ai.planning.Planner;
import com.gsir.ai.reasoning.ReasoningEngine;
import com.gsir.experiments.service.ExperimentService;
import com.gsir.knowledge.service.KnowledgeService;
import com.gsir.learning.service.LearningLoopService;
import com.gsir.projects.ProjectAccessService;
import com.gsir.validation.engine.ScientificValidationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class Part100ResearchLoopTest {

    // Removed ProjectRepository
    
    private ProjectAccessService projectAccessService;
    @Mock
    private Planner planner;
    @Mock
    private ReasoningEngine reasoningEngine;
    // Removed unused mocks to avoid mockito-inline Java 25 issues
    // private MetacognitionService metacognitionService;
    // private KnowledgeService knowledgeService;
    // private LearningLoopService learningLoopService;
    // private ExperimentService experimentService;
    // private ScientificValidationEngine validationEngine;
    
    @Mock
    private AgentExecutor agentExecutor;

    private DefaultScientificResearchLoop researchLoop;

    private UUID projectId;
    private UUID userId;
    private ExecutionState mockInitialState;
    private ExecutionState mockResumeState;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
        projectAccessService = new ProjectAccessService(null) {
            @Override
            public com.gsir.projects.ProjectEntity requireAccess(UUID p, UUID u) {
                return null;
            }
        };
        
        researchLoop = new DefaultScientificResearchLoop(
            projectAccessService, planner, reasoningEngine, null,
            null, null, null, null, agentExecutor
        );

        Plan mockPlan = new Plan(UUID.randomUUID(), UUID.randomUUID(), java.util.List.of(), new com.gsir.ai.planning.ExecutionConstraints(10, 10, 10, 10, 10, 10, 10));
        
        mockInitialState = new ExecutionState(
                UUID.randomUUID(), "Test Objective", mockPlan, UUID.randomUUID(),
                new HashMap<>(), new HashMap<>(), 5, "INITIALIZED", projectId, userId
        );

        mockResumeState = new ExecutionState(
                mockInitialState.executionId(), mockInitialState.objective(), mockInitialState.plan(), mockInitialState.currentStepId(),
                new HashMap<>(), new HashMap<>(), 4, "CONTINUE", projectId, userId
        );
    }

    @Test
    void testExecuteLoop_Success() {
        when(agentExecutor.executeTask(anyString(), eq(projectId), eq(userId), any(com.gsir.ai.execution.ExecutionBudget.class)))
                .thenReturn(mockInitialState);

        when(agentExecutor.resumeExecution(any(ExecutionState.class)))
                .thenReturn(mockResumeState);

        ResearchLoopState state = researchLoop.executeLoop(projectId, userId, "What is the nature of gravity?", 5);

        assertNotNull(state);
        assertEquals("COMPLETED", state.getStatus());
        assertTrue(state.isComplete());
        assertEquals("Synthesized known knowledge: What is the nature of gravity?", state.getConclusion());
    }

    @Test
    void testExecuteLoop_NeedsEvidence() {
        when(agentExecutor.executeTask(anyString(), eq(projectId), eq(userId), any(com.gsir.ai.execution.ExecutionBudget.class)))
                .thenReturn(mockInitialState);

        ExecutionState evidenceState = new ExecutionState(
                mockInitialState.executionId(), mockInitialState.objective(), mockInitialState.plan(), mockInitialState.currentStepId(),
                new HashMap<>(), new HashMap<>(), 4, "NEEDS_EVIDENCE", projectId, userId
        );

        when(agentExecutor.resumeExecution(any(ExecutionState.class)))
                .thenReturn(evidenceState);

        ResearchLoopState state = researchLoop.executeLoop(projectId, userId, "What is the nature of gravity?", 5);

        assertNotNull(state);
        assertEquals("INSUFFICIENT_EVIDENCE", state.getStatus());
        assertFalse(state.isComplete());
    }

    @Test
    void testExecuteLoop_NeedsToolUse_Halted() {
        // Since experiments are a limitation in GSIR (Part 66 incomplete), we halt when we need tool use but lack it.
        when(agentExecutor.executeTask(anyString(), eq(projectId), eq(userId), any(com.gsir.ai.execution.ExecutionBudget.class)))
                .thenReturn(mockInitialState);

        ExecutionState toolState = new ExecutionState(
                mockInitialState.executionId(), mockInitialState.objective(), mockInitialState.plan(), mockInitialState.currentStepId(),
                new HashMap<>(), new HashMap<>(), 4, "NEEDS_TOOL_USE", projectId, userId
        );

        when(agentExecutor.resumeExecution(any(ExecutionState.class)))
                .thenReturn(toolState);

        ResearchLoopState state = researchLoop.executeLoop(projectId, userId, "What is the nature of gravity?", 5);

        assertNotNull(state);
        assertEquals("NEEDS_HUMAN_REVIEW", state.getStatus());
        assertFalse(state.isComplete());
    }
}
