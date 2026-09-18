package com.gsir.learning.service;

import com.gsir.common.error.GsirException;
import com.gsir.knowledge.model.ClaimType;
import com.gsir.knowledge.model.EvidenceLink;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.service.KnowledgeService;
import com.gsir.learning.model.KnowledgeUpdate;
import com.gsir.learning.model.LearningSignal;
import com.gsir.projects.ProjectAccessService;
import com.gsir.provenance.service.ExecutionProvenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LearningLoopServiceTest {

    private KnowledgeService knowledgeService;
    private ProjectAccessService projectAccessService;
    private ExecutionProvenanceService provenanceService;
    private LearningLoopService learningLoopService;

    private boolean shouldThrowAuth = false;

    @BeforeEach
    void setUp() {
        knowledgeService = mock(KnowledgeService.class);
        provenanceService = new ExecutionProvenanceService(null) {
            @Override
            public UUID recordStepExecution(UUID workflowId, UUID stepId, String capability, String toolVersion, String inputs, String outputs, UUID userId, UUID projectId) {
                return UUID.randomUUID();
            }
        };

        projectAccessService = new ProjectAccessService(null) {
            @Override
            public com.gsir.projects.ProjectEntity requireAccess(UUID projectId, UUID userId) {
                if (shouldThrowAuth) {
                    throw new GsirException("FORBIDDEN", "No access", org.springframework.http.HttpStatus.FORBIDDEN);
                }
                return new com.gsir.projects.ProjectEntity(projectId, UUID.randomUUID(), "Test", "Desc");
            }
        };
        
        learningLoopService = new LearningLoopService(knowledgeService, projectAccessService, provenanceService);
    }

    @Test
    void testProcessLearningSignal_Success() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID evidenceDocId = UUID.randomUUID();
        
        KnowledgeUpdate update = new KnowledgeUpdate(
                UUID.randomUUID(), "RELATES_TO", UUID.randomUUID(),
                "New observed fact", ClaimType.OBSERVED_FACT, evidenceDocId, 0.95
        );
        
        LearningSignal signal = new LearningSignal(
                UUID.randomUUID(),
                "Observed A but expected B",
                "Expected B",
                "Tool run X",
                "Got A",
                "Mismatch detected",
                "Error is A-B",
                List.of(update),
                List.of("Maybe A is caused by C"),
                runId,
                projectId,
                userId
        );

        UUID mockProvenanceId = UUID.randomUUID();
        
        ScientificClaim mockClaim1 = new ScientificClaim(UUID.randomUUID(), "New observed fact", ClaimType.OBSERVED_FACT, null, null, null, null, null, null, null, null, null, null, null, null, null);
        ScientificClaim mockClaim2 = new ScientificClaim(UUID.randomUUID(), "Maybe A is caused by C", ClaimType.HYPOTHESIS, null, null, null, null, null, null, null, null, null, null, null, null, null);
        
        when(knowledgeService.createClaim(eq("New observed fact"), eq(ClaimType.OBSERVED_FACT), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockClaim1);
        when(knowledgeService.createClaim(eq("Maybe A is caused by C"), eq(ClaimType.HYPOTHESIS), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockClaim2);

        List<ScientificClaim> claims = learningLoopService.processLearningSignal(signal);
        
        assertThat(claims).hasSize(2);
        
        ArgumentCaptor<EvidenceLink> linkCaptor = ArgumentCaptor.forClass(EvidenceLink.class);
        verify(knowledgeService).attachEvidence(eq(mockClaim1.claimId()), linkCaptor.capture(), eq(projectId), eq(userId));
        
        EvidenceLink capturedLink = linkCaptor.getValue();
        assertThat(capturedLink.sourceDocumentId()).isEqualTo(evidenceDocId);
    }

    @Test
    void testProcessLearningSignal_MissingObservation() {
        LearningSignal signal = new LearningSignal(
                UUID.randomUUID(), null, null, null, null, null, "Error", null, null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
        
        assertThatThrownBy(() -> learningLoopService.processLearningSignal(signal))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("observation");
    }

    @Test
    void testProcessLearningSignal_MissingPredictionError() {
        LearningSignal signal = new LearningSignal(
                UUID.randomUUID(), "Obs", null, null, null, null, null, null, null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
        
        assertThatThrownBy(() -> learningLoopService.processLearningSignal(signal))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("prediction error");
    }

    @Test
    void testProcessLearningSignal_Unauthorized() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LearningSignal signal = new LearningSignal(
                UUID.randomUUID(), "Obs", "Pred", "Act", "Res", "Eval", "Err", null, null, UUID.randomUUID(), projectId, userId
        );
        
        shouldThrowAuth = true;
        
        assertThatThrownBy(() -> learningLoopService.processLearningSignal(signal))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("No access");
    }

    @Test
    void testProcessLearningSignal_FactWithoutEvidenceRejected() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        KnowledgeUpdate update = new KnowledgeUpdate(
                UUID.randomUUID(), "RELATES_TO", UUID.randomUUID(),
                "New unproven fact", ClaimType.OBSERVED_FACT, null, 0.95
        );
        
        LearningSignal signal = new LearningSignal(
                UUID.randomUUID(), "Obs", "Pred", "Act", "Res", "Eval", "Err", List.of(update), null, UUID.randomUUID(), projectId, userId
        );
        
        
        assertThatThrownBy(() -> learningLoopService.processLearningSignal(signal))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("Cannot insert OBSERVED_FACT without an evidence document reference");
    }
}
