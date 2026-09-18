package com.gsir.ai.improvement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.error.GsirException;
import com.gsir.provenance.service.ExecutionProvenanceService;
import com.gsir.execution.service.HumanReviewService;
import com.gsir.projects.ProjectAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SelfImprovementServiceTest {

    private ProjectAccessService projectAccessService;
    private ExecutionProvenanceService provenanceService;
    private HumanReviewService humanReviewService;
    private SelfImprovementService service;

    @BeforeEach
    void setUp() {
        projectAccessService = new ProjectAccessService(null) {
            @Override
            public com.gsir.projects.ProjectEntity requireAccess(UUID projectId, UUID userId) {
                return new com.gsir.projects.ProjectEntity(projectId, userId, "Test", "Test");
            }
        };
        provenanceService = new ExecutionProvenanceService(null) {
            @Override
            public UUID recordStepExecution(UUID runId, UUID stepId, String action, String version, String details, String outcome, UUID projectId, UUID dataObjectId) {
                return UUID.randomUUID();
            }
        };
        humanReviewService = new HumanReviewService(null, null, null) {
            @Override
            public com.gsir.execution.model.HumanReviewTaskEntity createReviewTask(UUID projectId, UUID aiRunId, UUID stepId, java.util.Map<String, Object> context) {
                return null;
            }
        };
        service = new SelfImprovementService(projectAccessService, provenanceService, humanReviewService, new ObjectMapper());
    }

    @Test
    void testProposeImprovement_MissingBaseline() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID aiRunId = UUID.randomUUID();

        SelfImprovementProposal proposal = new SelfImprovementProposal(
            UUID.randomUUID(), projectId, SelfImprovementCategory.PROMPT_CONFIGURATION,
            "Optimize reasoning", null, "New Prompt", "Faster", "Test X", "Revert"
        );

        SelfImprovementEvaluationResult result = service.proposeImprovement(proposal, userId, aiRunId);
        
        assertFalse(result.approved());
        assertFalse(result.requiresHumanReview());
        assertEquals("Baseline is required for any self-improvement proposal", result.reason());
    }

    @Test
    void testProposeImprovement_SourceCode_RequiresReview() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID aiRunId = UUID.randomUUID();

        SelfImprovementProposal proposal = new SelfImprovementProposal(
            UUID.randomUUID(), projectId, SelfImprovementCategory.SOURCE_CODE,
            "Rewrite sorting", "baseline-123", "New Code", "Faster", "Test X", "Revert"
        );

        SelfImprovementEvaluationResult result = service.proposeImprovement(proposal, userId, aiRunId);
        
        assertFalse(result.approved());
        assertTrue(result.requiresHumanReview());
    }

    @Test
    void testProposeImprovement_Strategy_Allowed() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID aiRunId = UUID.randomUUID();

        SelfImprovementProposal proposal = new SelfImprovementProposal(
            UUID.randomUUID(), projectId, SelfImprovementCategory.RESEARCH_STRATEGY,
            "Better logic", "baseline-123", "New Strategy", "Faster", "Test X", "Revert"
        );

        SelfImprovementEvaluationResult result = service.proposeImprovement(proposal, userId, aiRunId);
        
        assertTrue(result.approved());
        assertFalse(result.requiresHumanReview());
        assertFalse(result.regressionDetected());
    }

    @Test
    void testProposeImprovement_RegressionDetected() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID aiRunId = UUID.randomUUID();

        SelfImprovementProposal proposal = new SelfImprovementProposal(
            UUID.randomUUID(), projectId, SelfImprovementCategory.RESEARCH_STRATEGY,
            "MALICIOUS bypass", "baseline-123", "MALICIOUS", "Faster", "Test X", "Revert"
        );

        SelfImprovementEvaluationResult result = service.proposeImprovement(proposal, userId, aiRunId);
        
        assertFalse(result.approved());
        assertTrue(result.regressionDetected());
    }
}
