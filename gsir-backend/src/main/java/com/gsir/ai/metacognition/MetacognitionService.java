package com.gsir.ai.metacognition;

import com.gsir.ai.execution.ExecutionState;
import com.gsir.ai.reasoning.ReasoningResult;
import com.gsir.common.error.CommonErrors;
import com.gsir.projects.ProjectAccessService;
import com.gsir.provenance.service.ExecutionProvenanceService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MetacognitionService {

    private final ProjectAccessService projectAccessService;
    private final ExecutionProvenanceService provenanceService;

    public MetacognitionService(ProjectAccessService projectAccessService, ExecutionProvenanceService provenanceService) {
        this.projectAccessService = projectAccessService;
        this.provenanceService = provenanceService;
    }

    public MetaAssessment evaluate(ExecutionState state, ReasoningResult reasoningResult, UUID userId, UUID projectId) {
        // 1. Tenant boundary
        projectAccessService.requireAccess(projectId, userId);

        // 2. Assess
        double systemConfidence = com.gsir.common.math.NumericSafety.requireFinite(reasoningResult.confidence());
        double uncertainty = 1.0 - systemConfidence;
        
        boolean requiresMoreEvidence = false;
        boolean requiresToolUse = false;
        boolean requiresHumanReview = false;

        // Evidence check
        if (reasoningResult.evidenceCitations() == null || reasoningResult.evidenceCitations().isEmpty()) {
            requiresMoreEvidence = true;
            uncertainty = Math.max(uncertainty, 0.8);
            systemConfidence = Math.min(systemConfidence, 0.2);
        }

        // Missing info -> Tool use
        if (reasoningResult.missingInformation() != null && !reasoningResult.missingInformation().isEmpty()) {
            requiresToolUse = true;
        }

        // Contradictions -> Human review or more evidence
        if (reasoningResult.contradictions() != null && !reasoningResult.contradictions().isEmpty()) {
            requiresHumanReview = true;
            requiresMoreEvidence = true;
            uncertainty = Math.max(uncertainty, 0.9);
            systemConfidence = Math.min(systemConfidence, 0.1);
        }

        // Bounded reflection constraints: If we are low on budget, maybe escalate?
        if (state.remainingBudget() <= 1 && (requiresMoreEvidence || requiresToolUse)) {
            requiresHumanReview = true;
        }

        // 3. Provenance
        String assessmentJson = String.format("{\"confidence\":%f,\"uncertainty\":%f,\"requiresMoreEvidence\":%b,\"requiresHumanReview\":%b}", 
                systemConfidence, uncertainty, requiresMoreEvidence, requiresHumanReview);
        
        provenanceService.recordStepExecution(
                state.executionId(),
                state.currentStepId() != null ? state.currentStepId() : UUID.randomUUID(),
                "METACOGNITIVE_EVALUATION",
                "1.0",
                reasoningResult.conclusion(),
                assessmentJson,
                userId,
                projectId
        );

        return new MetaAssessment(
                systemConfidence,
                uncertainty,
                requiresMoreEvidence,
                requiresToolUse,
                requiresHumanReview
        );
    }
}

