package com.gsir.learning.service;

import com.gsir.common.error.CommonErrors;
import com.gsir.knowledge.model.ClaimType;
import com.gsir.knowledge.model.EvidenceLink;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.service.KnowledgeService;
import com.gsir.learning.model.KnowledgeUpdate;
import com.gsir.learning.model.LearningSignal;
import com.gsir.projects.ProjectAccessService;
import com.gsir.provenance.service.ExecutionProvenanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LearningLoopService {

    private final KnowledgeService knowledgeService;
    private final ProjectAccessService projectAccessService;
    private final ExecutionProvenanceService provenanceService;

    public LearningLoopService(KnowledgeService knowledgeService, ProjectAccessService projectAccessService, ExecutionProvenanceService provenanceService) {
        this.knowledgeService = knowledgeService;
        this.projectAccessService = projectAccessService;
        this.provenanceService = provenanceService;
    }

    @Transactional
    public List<ScientificClaim> processLearningSignal(LearningSignal signal) {
        // 1. Tenant boundary
        projectAccessService.requireAccess(signal.projectId(), signal.userId());
        
        // 2. Validate Learning Signal
        if (signal.observation() == null || signal.observation().isBlank()) {
            throw CommonErrors.invalid("Learning signal must contain an observation");
        }
        if (signal.predictionError() == null || signal.predictionError().isBlank()) {
            throw CommonErrors.invalid("Learning signal must contain a prediction error evaluation");
        }

        // 3. Provenance tracking for the learning event
        UUID workflowId = signal.aiRunId() != null ? signal.aiRunId() : UUID.randomUUID();
        UUID stepId = signal.signalId() != null ? signal.signalId() : UUID.randomUUID();
        
        UUID provenanceId = provenanceService.recordStepExecution(
            workflowId, 
            stepId, 
            "LEARNING_LOOP", "1.0", 
            signal.observation(), signal.predictionError(), 
            signal.userId(), signal.projectId()
        );

        List<ScientificClaim> generatedClaims = new ArrayList<>();

        // 4. Update Knowledge Graph based on the learning signal
        if (signal.knowledgeUpdates() != null) {
            for (KnowledgeUpdate update : signal.knowledgeUpdates()) {
                
                // Safety check: Cannot create OBSERVED_FACT directly from learning without evidence source
                if (update.type() == ClaimType.OBSERVED_FACT && update.evidenceDocumentId() == null) {
                    throw CommonErrors.invalid("Cannot insert OBSERVED_FACT without an evidence document reference.");
                }

                ScientificClaim claim = knowledgeService.createClaim(
                    update.statement(),
                    update.type(),
                    update.subjectId(),
                    update.predicate(),
                    update.objectId(),
                    provenanceId,
                    signal.projectId(),
                    signal.userId()
                );
                
                if (update.evidenceDocumentId() != null) {
                    EvidenceLink link = new EvidenceLink(
                        UUID.randomUUID(),
                        update.evidenceDocumentId(),
                        "LEARNING_LOOP_OBSERVATION",
                        update.confidence() != null ? update.confidence() : 1.0,
                        "1.0"
                    );
                    knowledgeService.attachEvidence(claim.claimId(), link, signal.projectId(), signal.userId());
                }
                
                generatedClaims.add(claim);
            }
        }
        
        // 5. Consolidate New Hypotheses based on prediction error
        if (signal.newHypotheses() != null) {
            for (String hypothesis : signal.newHypotheses()) {
                ScientificClaim claim = knowledgeService.createClaim(
                    hypothesis,
                    ClaimType.HYPOTHESIS,
                    null, "HYPOTHESIZES", null,
                    provenanceId,
                    signal.projectId(),
                    signal.userId()
                );
                generatedClaims.add(claim);
            }
        }
        
        return generatedClaims;
    }
}

