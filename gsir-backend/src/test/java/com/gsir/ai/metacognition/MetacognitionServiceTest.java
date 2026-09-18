package com.gsir.ai.metacognition;

import com.gsir.ai.execution.ExecutionState;
import com.gsir.ai.planning.Plan;
import com.gsir.ai.planning.ExecutionConstraints;
import com.gsir.ai.reasoning.ReasoningResult;
import com.gsir.common.error.GsirException;
import com.gsir.projects.ProjectAccessService;
import com.gsir.projects.ProjectEntity;
import com.gsir.provenance.service.ExecutionProvenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetacognitionServiceTest {

    private ProjectAccessService projectAccessService;
    private ExecutionProvenanceService provenanceService;
    private MetacognitionService metacognitionService;

    private boolean shouldThrowAuth = false;

    @BeforeEach
    void setUp() {
        shouldThrowAuth = false;

        projectAccessService = new ProjectAccessService(null) {
            @Override
            public ProjectEntity requireAccess(UUID projectId, UUID userId) {
                if (shouldThrowAuth) {
                    throw new GsirException("FORBIDDEN", "No access", HttpStatus.FORBIDDEN);
                }
                return new ProjectEntity(projectId, UUID.randomUUID(), "Test", "Desc");
            }
        };

        provenanceService = new ExecutionProvenanceService(null) {
            @Override
            public UUID recordStepExecution(UUID workflowId, UUID stepId, String capability, String toolVersion, String inputs, String outputs, UUID userId, UUID projectId) {
                return UUID.randomUUID();
            }
        };

        metacognitionService = new MetacognitionService(projectAccessService, provenanceService);
    }

    @Test
    void testEvaluate_SufficientEvidence() {
        ExecutionState state = new ExecutionState(
                UUID.randomUUID(), "Goal", new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(), new ExecutionConstraints(1, 1L, 1.0, 1, 1, 1L, 1)), UUID.randomUUID(),
                new HashMap<>(), new HashMap<>(), 10, "RUNNING", UUID.randomUUID(), UUID.randomUUID()
        );

        ReasoningResult result = new ReasoningResult(
                "Conclusion reached.", 0.9, List.of("doc123"), List.of(), List.of(), null, 0.0, false
        );

        MetaAssessment assessment = metacognitionService.evaluate(state, result, state.userId(), state.projectId());

        assertThat(assessment.confidence()).isEqualTo(0.9);
        assertThat(assessment.uncertainty()).isCloseTo(0.1, org.assertj.core.data.Offset.offset(0.0001));
        assertThat(assessment.requiresMoreEvidence()).isFalse();
        assertThat(assessment.requiresToolUse()).isFalse();
        assertThat(assessment.requiresHumanReview()).isFalse();
    }

    @Test
    void testEvaluate_MissingEvidence() {
        ExecutionState state = new ExecutionState(
                UUID.randomUUID(), "Goal", new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(), new ExecutionConstraints(1, 1L, 1.0, 1, 1, 1L, 1)), UUID.randomUUID(),
                new HashMap<>(), new HashMap<>(), 10, "RUNNING", UUID.randomUUID(), UUID.randomUUID()
        );

        ReasoningResult result = new ReasoningResult(
                "Conclusion guessed.", 0.8, List.of(), List.of(), List.of(), null, 0.0, false
        );

        MetaAssessment assessment = metacognitionService.evaluate(state, result, state.userId(), state.projectId());

        assertThat(assessment.requiresMoreEvidence()).isTrue();
        assertThat(assessment.confidence()).isLessThanOrEqualTo(0.2);
        assertThat(assessment.uncertainty()).isGreaterThanOrEqualTo(0.8);
    }

    @Test
    void testEvaluate_MissingInformation_TriggersTool() {
        ExecutionState state = new ExecutionState(
                UUID.randomUUID(), "Goal", new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(), new ExecutionConstraints(1, 1L, 1.0, 1, 1, 1L, 1)), UUID.randomUUID(),
                new HashMap<>(), new HashMap<>(), 10, "RUNNING", UUID.randomUUID(), UUID.randomUUID()
        );

        ReasoningResult result = new ReasoningResult(
                "Need data.", 0.6, List.of("doc123"), List.of(), List.of("Calculation of X"), null, 0.0, false
        );

        MetaAssessment assessment = metacognitionService.evaluate(state, result, state.userId(), state.projectId());

        assertThat(assessment.requiresToolUse()).isTrue();
    }

    @Test
    void testEvaluate_Contradictions_TriggersReview() {
        ExecutionState state = new ExecutionState(
                UUID.randomUUID(), "Goal", new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(), new ExecutionConstraints(1, 1L, 1.0, 1, 1, 1L, 1)), UUID.randomUUID(),
                new HashMap<>(), new HashMap<>(), 10, "RUNNING", UUID.randomUUID(), UUID.randomUUID()
        );

        ReasoningResult result = new ReasoningResult(
                "Conflicts found.", 0.5, List.of("doc1", "doc2"), List.of("doc1 says X, doc2 says Y"), List.of(), null, 0.0, false
        );

        MetaAssessment assessment = metacognitionService.evaluate(state, result, state.userId(), state.projectId());

        assertThat(assessment.requiresHumanReview()).isTrue();
        assertThat(assessment.requiresMoreEvidence()).isTrue();
        assertThat(assessment.confidence()).isLessThanOrEqualTo(0.1);
        assertThat(assessment.uncertainty()).isGreaterThanOrEqualTo(0.9);
    }

    @Test
    void testEvaluate_BudgetExhaustion_TriggersReview() {
        ExecutionState state = new ExecutionState(
                UUID.randomUUID(), "Goal", new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(), new ExecutionConstraints(1, 1L, 1.0, 1, 1, 1L, 1)), UUID.randomUUID(),
                new HashMap<>(), new HashMap<>(), 1, "RUNNING", UUID.randomUUID(), UUID.randomUUID()
        );

        ReasoningResult result = new ReasoningResult(
                "Need more data.", 0.6, List.of(), List.of(), List.of(), null, 0.0, false // Missing evidence
        );

        MetaAssessment assessment = metacognitionService.evaluate(state, result, state.userId(), state.projectId());

        // Budget is low (1), and requiresMoreEvidence is true due to missing evidence -> triggers human review.
        assertThat(assessment.requiresHumanReview()).isTrue();
    }

    @Test
    void testEvaluate_Unauthorized() {
        shouldThrowAuth = true;
        
        ExecutionState state = new ExecutionState(
                UUID.randomUUID(), "Goal", new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(), new ExecutionConstraints(1, 1L, 1.0, 1, 1, 1L, 1)), UUID.randomUUID(),
                new HashMap<>(), new HashMap<>(), 10, "RUNNING", UUID.randomUUID(), UUID.randomUUID()
        );

        ReasoningResult result = new ReasoningResult(
                "Conclusion reached.", 0.9, List.of("doc123"), List.of(), List.of(), null, 0.0, false
        );

        assertThatThrownBy(() -> metacognitionService.evaluate(state, result, state.userId(), state.projectId()))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("No access");
    }
}
