package com.gsir.validation.service;

import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.model.ValidationStatus;
import com.gsir.knowledge.service.KnowledgeService;
import com.gsir.projects.ProjectAccessService;
import com.gsir.provenance.service.ExecutionProvenanceService;
import com.gsir.validation.engine.ScientificValidationEngine;
import com.gsir.validation.model.ValidationResult;
import com.gsir.common.science.EvidenceStatus;
import com.gsir.common.error.CommonErrors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClaimValidationService {

    private final ScientificValidationEngine validationEngine;
    private final KnowledgeService knowledgeService;
    private final ProjectAccessService projectAccessService;
    private final ExecutionProvenanceService provenanceService;

    public ClaimValidationService(
            ScientificValidationEngine validationEngine,
            KnowledgeService knowledgeService,
            ProjectAccessService projectAccessService,
            ExecutionProvenanceService provenanceService) {
        this.validationEngine = validationEngine;
        this.knowledgeService = knowledgeService;
        this.projectAccessService = projectAccessService;
        this.provenanceService = provenanceService;
    }

    @Transactional
    public ScientificClaim validateClaim(ScientificClaim claim, UUID workflowId, UUID stepId, UUID userId) {
        projectAccessService.requireAccess(claim.projectId(), userId);

        ValidationResult result = validationEngine.validate(claim);

        ValidationStatus newStatus;
        if (result.valid()) {
            newStatus = ValidationStatus.VALIDATED;
        } else if (result.status() == EvidenceStatus.CONFLICTING) {
            newStatus = ValidationStatus.PENDING_REVIEW;
        } else {
            newStatus = ValidationStatus.REJECTED;
        }

        // Update Claim Validation Status (Assume knowledgeService has this method or we will add it)
        ScientificClaim updatedClaim = knowledgeService.updateClaimValidationStatus(
                claim.claimId(), newStatus, claim.projectId(), userId);

        // Record Provenance
        provenanceService.recordStepExecution(
                workflowId,
                stepId != null ? stepId : UUID.randomUUID(),
                "SCIENTIFIC_VALIDATION",
                "1.0",
                "Validated claim " + claim.claimId(),
                "Result: " + newStatus.name() + ". Errors: " + String.join(", ", result.errors()),
                userId,
                claim.projectId()
        );

        return updatedClaim;
    }
}

