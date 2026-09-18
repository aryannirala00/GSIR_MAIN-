package com.gsir.ai.improvement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.provenance.service.ExecutionProvenanceService;
import com.gsir.execution.service.HumanReviewService;
import com.gsir.projects.ProjectAccessService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class SelfImprovementService {

    private final ProjectAccessService projectAccessService;
    private final ExecutionProvenanceService provenanceService;
    private final HumanReviewService humanReviewService;
    private final ObjectMapper objectMapper;

    public SelfImprovementService(ProjectAccessService projectAccessService,
                                  ExecutionProvenanceService provenanceService,
                                  HumanReviewService humanReviewService,
                                  ObjectMapper objectMapper) {
        this.projectAccessService = projectAccessService;
        this.provenanceService = provenanceService;
        this.humanReviewService = humanReviewService;
        this.objectMapper = objectMapper;
    }

    public SelfImprovementEvaluationResult proposeImprovement(SelfImprovementProposal proposal, UUID userId, UUID aiRunId) {
        // 1. Tenant Isolation & Authorization
        projectAccessService.requireAccess(proposal.projectId(), userId);

        // 2. Validate Baseline Presence
        if (proposal.baselineId() == null || proposal.baselineId().isBlank()) {
            return new SelfImprovementEvaluationResult(false, false, false, "Baseline is required for any self-improvement proposal", "{}");
        }

        // 3. Safety Boundary & Category Rules
        boolean requiresReview = false;
        boolean prohibited = false;
        
        switch (proposal.category()) {
            case SOURCE_CODE:
            case SECURITY_POLICY:
            case DEPLOYMENT:
            case MODEL_WEIGHTS:
                requiresReview = true;
                break;
            case KNOWLEDGE_REPRESENTATION:
            case RESEARCH_STRATEGY:
            case PROMPT_CONFIGURATION:
            case TOOL_SELECTION:
            case REASONING_STRATEGY:
            case MODEL_ROUTING:
                // Allowed with validation
                break;
            default:
                prohibited = true;
        }

        if (prohibited) {
            recordProvenance(proposal, userId, aiRunId, "DENIED - Prohibited Category");
            return new SelfImprovementEvaluationResult(false, false, false, "Category is prohibited from autonomous improvement", "{}");
        }
        
        // 4. Regression Detection (Mock validation logic)
        // In reality, this would execute an A/B test or validate against an evaluation dataset.
        boolean regressionDetected = checkRegression(proposal);
        if (regressionDetected) {
            recordProvenance(proposal, userId, aiRunId, "DENIED - Regression Detected");
            return new SelfImprovementEvaluationResult(false, false, true, "Regression detected during evaluation against baseline", "{}");
        }

        // 5. Human Review
        if (requiresReview) {
            humanReviewService.createReviewTask(proposal.projectId(), aiRunId, proposal.id(), Map.of(
                "message", "Review required for self-improvement: " + proposal.category(),
                "baselineId", proposal.baselineId(),
                "candidatePayload", proposal.candidatePayload()
            ));
            recordProvenance(proposal, userId, aiRunId, "PENDING_HUMAN_REVIEW");
            return new SelfImprovementEvaluationResult(false, true, false, "Human review required for this improvement category", "{}");
        }

        // 6. Approved for Automatic Promotion
        recordProvenance(proposal, userId, aiRunId, "APPROVED");
        return new SelfImprovementEvaluationResult(true, false, false, "Improvement validated and approved", "{}");
    }
    
    private boolean checkRegression(SelfImprovementProposal proposal) {
        // Implement mock multi-objective regression check.
        // It must check accuracy, cost, latency, safety.
        // For demonstration, if payload contains "MALICIOUS" we flag it.
        if (proposal.candidatePayload() != null && proposal.candidatePayload().contains("MALICIOUS")) {
            return true;
        }
        return false;
    }

    private void recordProvenance(SelfImprovementProposal proposal, UUID userId, UUID aiRunId, String status) {
        try {
            String details = objectMapper.writeValueAsString(proposal);
            provenanceService.recordStepExecution(
                aiRunId, proposal.id(), "SELF_IMPROVEMENT_" + proposal.category().name(), "1.0",
                details, "STATUS: " + status, proposal.projectId(), null
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to record provenance for self improvement", e);
        }
    }
}
