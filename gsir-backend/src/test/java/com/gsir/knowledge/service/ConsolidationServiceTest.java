package com.gsir.knowledge.service;

import com.gsir.common.error.GsirException;
import com.gsir.knowledge.model.CandidateInformation;
import com.gsir.knowledge.model.ClaimStatus;
import com.gsir.knowledge.model.ClaimType;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.model.EvidenceLink;
import com.gsir.projects.ProjectAccessService;
import com.gsir.projects.ProjectEntity;
import com.gsir.provenance.service.ExecutionProvenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConsolidationServiceTest {

    private ProjectAccessService projectAccessService;
    private ExecutionProvenanceService provenanceService;
    private KnowledgeService knowledgeService;
    private ContradictionDetectionService contradictionService;
    private ConsolidationService consolidationService;

    private boolean shouldThrowAuth = false;
    private List<ScientificClaim> existingClaims = Collections.emptyList();
    private List<ScientificClaim> contradictions = Collections.emptyList();

    @BeforeEach
    void setUp() {
        shouldThrowAuth = false;
        existingClaims = Collections.emptyList();
        contradictions = Collections.emptyList();

        projectAccessService = new ProjectAccessService(null) {
            @Override
            public ProjectEntity requireAccess(UUID projectId, UUID userId) {
                if (shouldThrowAuth) {
                    throw new GsirException("FORBIDDEN", "No access", org.springframework.http.HttpStatus.FORBIDDEN);
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

        knowledgeService = new KnowledgeService() {
            @Override
            public ScientificClaim createClaim(String statement, ClaimType type, UUID subject, String predicate, UUID object, UUID provenanceId, UUID projectId, UUID userId) {
                return new ScientificClaim(
                        UUID.randomUUID(), statement, type, subject, predicate, object,
                        Collections.emptyList(), Collections.emptyList(), provenanceId, null,
                        "GENERAL", 1.0, ClaimStatus.PROPOSED, null, projectId, 1
                );
            }
            @Override
            public ScientificClaim updateClaimValidationStatus(UUID claimId, com.gsir.knowledge.model.ValidationStatus status, UUID projectId, UUID userId) {
                return null;
            }
            @Override
            public void attachEvidence(UUID claimId, EvidenceLink evidence, UUID projectId, UUID userId) {}
            @Override
            public ScientificClaim updateClaimStatus(UUID claimId, ClaimStatus status, UUID projectId, UUID userId) {
                return new ScientificClaim(
                        claimId, "Mock", ClaimType.OBSERVED_FACT, UUID.randomUUID(), "pred", UUID.randomUUID(),
                        Collections.emptyList(), Collections.emptyList(), UUID.randomUUID(), null,
                        "GENERAL", 1.0, status, null, projectId, 1
                );
            }
            @Override
            public List<ScientificClaim> findClaimsBySubjectAndPredicate(UUID subject, String predicate, UUID projectId, UUID userId) {
                return existingClaims;
            }
            @Override
            public Optional<com.gsir.knowledge.model.KnowledgeEntity> resolveEntity(String name, UUID projectId, UUID userId) { return Optional.empty(); }
            @Override
            public com.gsir.knowledge.model.KnowledgeEntity createEntity(String type, String canonicalName, List<String> aliases, java.util.Map<String, Object> properties, String domain, UUID projectId, UUID userId) { return null; }
            @Override
            public Optional<com.gsir.knowledge.model.KnowledgeEntity> getEntity(UUID entityId, UUID projectId, UUID userId) { return Optional.empty(); }
            @Override
            public com.gsir.knowledge.model.KnowledgeEntity updateEntity(com.gsir.knowledge.model.KnowledgeEntity entity, UUID projectId, UUID userId) { return null; }
            @Override
            public com.gsir.knowledge.model.KnowledgeRelationship createRelationship(UUID source, UUID target, String type, java.util.Map<String, Object> properties, Double confidence, UUID provenanceId, UUID projectId, UUID userId) { return null; }
            @Override
            public List<com.gsir.knowledge.model.KnowledgeRelationship> getRelationships(UUID entityId, UUID projectId, UUID userId) { return Collections.emptyList(); }
            @Override
            public List<com.gsir.knowledge.model.KnowledgeEntity> searchKnowledge(String query, UUID projectId, UUID userId) { return Collections.emptyList(); }
            @Override
            public List<List<com.gsir.knowledge.model.KnowledgeRelationship>> traverseGraph(UUID startEntity, UUID targetEntity, int maxHops, UUID projectId, UUID userId) { return Collections.emptyList(); }
            @Override
            public List<ScientificClaim> runInference(UUID startEntityId, String ruleSet, UUID projectId, UUID userId) { return Collections.emptyList(); }
            @Override
            public List<ScientificClaim> detectConflict(UUID entityId, UUID projectId, UUID userId) { return Collections.emptyList(); }
            @Override
            public String createSnapshot(UUID projectId, UUID userId) { return ""; }
            @Override
            public String exportKnowledge(UUID projectId, UUID userId) { return ""; }
            @Override
            public void deprecateKnowledge(UUID objectId, UUID projectId, UUID userId) {}
            @Override
            public List<com.gsir.knowledge.model.KnowledgeRelationship> searchNeighborhood(UUID entityId, int depth, UUID projectId, UUID userId) { return Collections.emptyList(); }
        };

        contradictionService = new ContradictionDetectionService(null, null) {
            @Override
            public List<ScientificClaim> findContradictions(ScientificClaim claim, UUID projectId, UUID userId) {
                return contradictions;
            }
        };

        consolidationService = new ConsolidationService(projectAccessService, provenanceService, knowledgeService, contradictionService);
    }

    @Test
    void testConsolidate_Success() {
        CandidateInformation candidate = new CandidateInformation(
                "Water boils at 100C", ClaimType.OBSERVED_FACT, UUID.randomUUID(), "hasBoilingPoint", UUID.randomUUID(),
                List.of(new EvidenceLink(UUID.randomUUID(), UUID.randomUUID(), "SOURCE", 1.0, "1.0")),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        ScientificClaim result = consolidationService.consolidate(candidate);
        assertThat(result).isNotNull();
        assertThat(result.statement()).isEqualTo("Water boils at 100C");
    }

    @Test
    void testConsolidate_MissingEvidence_RejectsObservedFact() {
        CandidateInformation candidate = new CandidateInformation(
                "Water boils at 100C", ClaimType.OBSERVED_FACT, UUID.randomUUID(), "hasBoilingPoint", UUID.randomUUID(),
                null,
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        assertThatThrownBy(() -> consolidationService.consolidate(candidate))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("Cannot consolidate an OBSERVED_FACT without evidence");
    }

    @Test
    void testConsolidate_Deduplication() {
        UUID existingId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID objectId = UUID.randomUUID();
        
        ScientificClaim existingClaim = new ScientificClaim(
                existingId, "Existing Fact", ClaimType.OBSERVED_FACT, subjectId, "hasProperty", objectId,
                Collections.emptyList(), Collections.emptyList(), UUID.randomUUID(), null,
                "GENERAL", 1.0, ClaimStatus.VERIFIED, null, UUID.randomUUID(), 1
        );
        existingClaims = List.of(existingClaim);

        CandidateInformation candidate = new CandidateInformation(
                "Existing Fact", ClaimType.OBSERVED_FACT, subjectId, "hasProperty", objectId,
                List.of(new EvidenceLink(UUID.randomUUID(), UUID.randomUUID(), "SOURCE", 1.0, "1.0")),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        ScientificClaim result = consolidationService.consolidate(candidate);
        
        assertThat(result.claimId()).isEqualTo(existingId);
    }

    @Test
    void testConsolidate_PreservesConflict() {
        ScientificClaim contradiction = new ScientificClaim(
                UUID.randomUUID(), "Water boils at 90C", ClaimType.OBSERVED_FACT, UUID.randomUUID(), "hasBoilingPoint", UUID.randomUUID(),
                Collections.emptyList(), Collections.emptyList(), UUID.randomUUID(), null,
                "GENERAL", 1.0, ClaimStatus.VERIFIED, null, UUID.randomUUID(), 1
        );
        contradictions = List.of(contradiction);

        CandidateInformation candidate = new CandidateInformation(
                "Water boils at 100C", ClaimType.OBSERVED_FACT, UUID.randomUUID(), "hasBoilingPoint", UUID.randomUUID(),
                List.of(new EvidenceLink(UUID.randomUUID(), UUID.randomUUID(), "SOURCE", 1.0, "1.0")),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        ScientificClaim result = consolidationService.consolidate(candidate);
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(ClaimStatus.CONTRADICTED);
    }

    @Test
    void testConsolidate_Unauthorized() {
        shouldThrowAuth = true;
        
        CandidateInformation candidate = new CandidateInformation(
                "Fact", ClaimType.OBSERVED_FACT, UUID.randomUUID(), "pred", UUID.randomUUID(),
                List.of(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        assertThatThrownBy(() -> consolidationService.consolidate(candidate))
                .isInstanceOf(com.gsir.common.error.GsirException.class)
                .hasMessageContaining("No access");
    }

    @Test
    void testConsolidate_InvalidInput_Fails() {
        CandidateInformation invalid = new CandidateInformation(
                null, ClaimType.OBSERVED_FACT, null, null, null,
                null, null, null, UUID.randomUUID(), UUID.randomUUID()
        );
        assertThatThrownBy(() -> consolidationService.consolidate(invalid))
                .isInstanceOf(com.gsir.common.error.GsirException.class)
                .hasMessageContaining("Cannot consolidate");
    }
}
