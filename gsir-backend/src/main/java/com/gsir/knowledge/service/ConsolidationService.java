package com.gsir.knowledge.service;

import com.gsir.knowledge.model.CandidateInformation;
import com.gsir.knowledge.model.ClaimStatus;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.model.EvidenceLink;
import com.gsir.projects.ProjectAccessService;
import com.gsir.provenance.service.ExecutionProvenanceService;
import com.gsir.common.error.CommonErrors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

@Service
public class ConsolidationService {

    private final ProjectAccessService projectAccessService;
    private final ExecutionProvenanceService provenanceService;
    private final KnowledgeService knowledgeService;
    private final ContradictionDetectionService contradictionDetectionService;
    public ConsolidationService(
            ProjectAccessService projectAccessService,
            ExecutionProvenanceService provenanceService,
            KnowledgeService knowledgeService,
            ContradictionDetectionService contradictionDetectionService) {
        this.projectAccessService = projectAccessService;
        this.provenanceService = provenanceService;
        this.knowledgeService = knowledgeService;
        this.contradictionDetectionService = contradictionDetectionService;
    }

    @Transactional
    public ScientificClaim consolidate(CandidateInformation candidate) {
        // 1. Tenant boundary
        projectAccessService.requireAccess(candidate.projectId(), candidate.userId());

        // 2. Validation / Evidence Check
        if (candidate.statement() == null || candidate.statement().isBlank()) {
            throw CommonErrors.invalid("Cannot consolidate a claim without a statement.");
        }
        if (candidate.subjectId() == null || candidate.predicate() == null) {
            throw CommonErrors.invalid("Cannot consolidate a claim without a subject and predicate.");
        }

        if (candidate.evidence() == null || candidate.evidence().isEmpty()) {
            if (candidate.claimType() == com.gsir.knowledge.model.ClaimType.OBSERVED_FACT) {
                throw CommonErrors.invalid("Cannot consolidate an OBSERVED_FACT without evidence.");
            }
        }

        // 3. Deduplication
        // Check if an identical claim already exists
        List<ScientificClaim> existingClaims = knowledgeService.findClaimsBySubjectAndPredicate(
                candidate.subjectId(), candidate.predicate(), candidate.projectId(), candidate.userId());
        
        Optional<ScientificClaim> duplicate = existingClaims.stream()
                .filter(c -> c.statement().equalsIgnoreCase(candidate.statement()) && java.util.Objects.equals(c.objectEntityId(), candidate.objectId()))
                .findFirst();

        if (duplicate.isPresent()) {
            ScientificClaim existing = duplicate.get();
            // Just attach new evidence and provenance to the existing claim
            UUID provId = provenanceService.recordStepExecution(
                    candidate.workflowId(),
                    candidate.stepId() != null ? candidate.stepId() : UUID.randomUUID(),
                    "CONSOLIDATION_DEDUPLICATION",
                    "1.0",
                    candidate.statement(),
                    "Mapped to existing claim: " + existing.claimId(),
                    candidate.userId(),
                    candidate.projectId()
            );
            
            if (candidate.evidence() != null) {
                for (EvidenceLink link : candidate.evidence()) {
                    knowledgeService.attachEvidence(existing.claimId(), link, candidate.projectId(), candidate.userId());
                }
            }
            return existing;
        }

        // 4. Conflict Check
        // Are there contradictory claims in the graph?
        // Let's create a temporary claim object to check
        ScientificClaim tempClaim = new ScientificClaim(
                UUID.randomUUID(), candidate.statement(), candidate.claimType(), candidate.subjectId(), candidate.predicate(), candidate.objectId(),
                candidate.evidence() != null ? candidate.evidence() : List.of(),
                List.of(), null, null, "GENERAL", 1.0, ClaimStatus.PROPOSED, null, candidate.projectId(), 1
        );

        List<ScientificClaim> contradictions = contradictionDetectionService.findContradictions(tempClaim, candidate.projectId(), candidate.userId());
        ClaimStatus finalStatus = ClaimStatus.SUPPORTED;
        
        if (!contradictions.isEmpty()) {
            finalStatus = ClaimStatus.CONTRADICTED;
            // "Conflict must be preserved. CONFLICTING != FALSE"
        } else if (candidate.claimType() == com.gsir.knowledge.model.ClaimType.HYPOTHESIS) {
            finalStatus = ClaimStatus.UNKNOWN; // Hypotheses start as UNKNOWN or HYPOTHESIS, not fact
        }

        // 5. Provenance
        UUID provenanceId = provenanceService.recordStepExecution(
                candidate.workflowId(),
                candidate.stepId() != null ? candidate.stepId() : UUID.randomUUID(),
                "CONSOLIDATION",
                "1.0",
                candidate.statement(),
                "Status: " + finalStatus.name(),
                candidate.userId(),
                candidate.projectId()
        );

        // 6. Final Consolidation
        ScientificClaim consolidatedClaim = knowledgeService.createClaim(
                candidate.statement(),
                candidate.claimType(),
                candidate.subjectId(),
                candidate.predicate(),
                candidate.objectId(),
                provenanceId,
                candidate.projectId(),
                candidate.userId()
        );

        if (candidate.evidence() != null) {
            for (EvidenceLink link : candidate.evidence()) {
                knowledgeService.attachEvidence(consolidatedClaim.claimId(), link, candidate.projectId(), candidate.userId());
            }
        }

        if (finalStatus != ClaimStatus.PROPOSED && finalStatus != ClaimStatus.SUPPORTED) {
            consolidatedClaim = knowledgeService.updateClaimStatus(consolidatedClaim.claimId(), finalStatus, candidate.projectId(), candidate.userId());
        }

        return consolidatedClaim;
    }
}
