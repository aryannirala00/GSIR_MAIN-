package com.gsir.validation.service;

import com.gsir.knowledge.model.ClaimStatus;
import com.gsir.knowledge.model.ClaimType;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.model.ValidationStatus;
import com.gsir.knowledge.service.KnowledgeService;
import com.gsir.projects.ProjectAccessService;
import com.gsir.provenance.service.ExecutionProvenanceService;
import com.gsir.validation.engine.ScientificValidationEngine;
import com.gsir.validation.engine.ScientificClaimValidator;
import com.gsir.common.error.GsirException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimValidationServiceTest {

    private ScientificValidationEngine engine;
    private KnowledgeService knowledgeService;
    private ProjectAccessService projectAccessService;
    private ExecutionProvenanceService provenanceService;
    private ClaimValidationService claimValidationService;

    private boolean shouldThrowAuth = false;

    @BeforeEach
    void setUp() {
        engine = new ScientificValidationEngine(List.of(new ScientificClaimValidator()));

        projectAccessService = new ProjectAccessService(null) {
            @Override
            public com.gsir.projects.ProjectEntity requireAccess(UUID projectId, UUID userId) {
                if (shouldThrowAuth) {
                    throw com.gsir.common.error.CommonErrors.forbidden();
                }
                return null;
            }
        };

        knowledgeService = new KnowledgeService() {
            @Override
            public com.gsir.knowledge.model.KnowledgeEntity createEntity(String type, String canonicalName, List<String> aliases, java.util.Map<String, Object> properties, String domain, UUID projectId, UUID userId) { return null; }
            @Override
            public ScientificClaim createClaim(String statement, ClaimType type, UUID subject, String predicate, UUID object, UUID provenanceId, UUID projectId, UUID userId) { return null; }
            @Override
            public ScientificClaim updateClaimStatus(UUID claimId, ClaimStatus status, UUID projectId, UUID userId) { return null; }
            @Override
            public ScientificClaim updateClaimValidationStatus(UUID claimId, ValidationStatus status, UUID projectId, UUID userId) {
                return new ScientificClaim(
                        claimId, "Test", ClaimType.HYPOTHESIS, UUID.randomUUID(), "pred", UUID.randomUUID(),
                        List.of(), List.of(), UUID.randomUUID(), null, "GENERAL", 1.0, ClaimStatus.UNKNOWN, status, projectId, 1
                );
            }
            @Override
            public void attachEvidence(UUID claimId, com.gsir.knowledge.model.EvidenceLink evidence, UUID projectId, UUID userId) {}
            @Override
            public List<ScientificClaim> findClaimsBySubjectAndPredicate(UUID subject, String predicate, UUID projectId, UUID userId) { return List.of(); }
            @Override
            public java.util.Optional<com.gsir.knowledge.model.KnowledgeEntity> resolveEntity(String name, UUID projectId, UUID userId) { return java.util.Optional.empty(); }
            @Override
            public List<com.gsir.knowledge.model.KnowledgeRelationship> searchNeighborhood(UUID entityId, int maxHops, UUID projectId, UUID userId) { return null; }
            @Override
            public void deprecateKnowledge(UUID objectId, UUID projectId, UUID userId) {}
            @Override
            public java.util.Optional<com.gsir.knowledge.model.KnowledgeEntity> getEntity(UUID entityId, UUID projectId, UUID userId) { return java.util.Optional.empty(); }
            @Override
            public com.gsir.knowledge.model.KnowledgeEntity updateEntity(com.gsir.knowledge.model.KnowledgeEntity entity, UUID projectId, UUID userId) { return null; }
            @Override
            public com.gsir.knowledge.model.KnowledgeRelationship createRelationship(UUID source, UUID target, String type, java.util.Map<String, Object> properties, Double confidence, UUID provenanceId, UUID projectId, UUID userId) { return null; }
            @Override
            public List<com.gsir.knowledge.model.KnowledgeRelationship> getRelationships(UUID entityId, UUID projectId, UUID userId) { return null; }
            @Override
            public List<com.gsir.knowledge.model.KnowledgeEntity> searchKnowledge(String query, UUID projectId, UUID userId) { return null; }
            @Override
            public List<List<com.gsir.knowledge.model.KnowledgeRelationship>> traverseGraph(UUID startEntity, UUID targetEntity, int maxHops, UUID projectId, UUID userId) { return null; }
            @Override
            public List<ScientificClaim> runInference(UUID startEntityId, String ruleSet, UUID projectId, UUID userId) { return null; }
            @Override
            public List<ScientificClaim> detectConflict(UUID entityId, UUID projectId, UUID userId) { return null; }
            @Override
            public String createSnapshot(UUID projectId, UUID userId) { return null; }
            @Override
            public String exportKnowledge(UUID projectId, UUID userId) { return null; }
        };

        provenanceService = new ExecutionProvenanceService(null) {
            @Override
            public UUID recordStepExecution(UUID workflowId, UUID stepId, String action, String toolVersion, String inputs, String outputs, UUID userId, UUID projectId) {
                return UUID.randomUUID();
            }
        };

        claimValidationService = new ClaimValidationService(engine, knowledgeService, projectAccessService, provenanceService);
    }

    @Test
    void testValidateClaim_Success() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ScientificClaim claim = new ScientificClaim(
                UUID.randomUUID(), "Valid statement", ClaimType.HYPOTHESIS, UUID.randomUUID(), "pred", UUID.randomUUID(),
                List.of(), List.of(), UUID.randomUUID(), null, "GENERAL", 1.0, ClaimStatus.SUPPORTED, ValidationStatus.UNVALIDATED, projectId, 1
        );

        ScientificClaim result = claimValidationService.validateClaim(claim, UUID.randomUUID(), UUID.randomUUID(), userId);

        assertThat(result).isNotNull();
        assertThat(result.validationStatus()).isEqualTo(ValidationStatus.VALIDATED);
    }

    @Test
    void testValidateClaim_Contradicted() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ScientificClaim claim = new ScientificClaim(
                UUID.randomUUID(), "Contradicted statement", ClaimType.HYPOTHESIS, UUID.randomUUID(), "pred", UUID.randomUUID(),
                List.of(), List.of(), UUID.randomUUID(), null, "GENERAL", 1.0, ClaimStatus.CONTRADICTED, ValidationStatus.UNVALIDATED, projectId, 1
        );

        ScientificClaim result = claimValidationService.validateClaim(claim, UUID.randomUUID(), UUID.randomUUID(), userId);

        assertThat(result).isNotNull();
        assertThat(result.validationStatus()).isEqualTo(ValidationStatus.PENDING_REVIEW);
    }
    
    @Test
    void testValidateClaim_InvalidConfidence() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ScientificClaim claim = new ScientificClaim(
                UUID.randomUUID(), "Invalid conf", ClaimType.HYPOTHESIS, UUID.randomUUID(), "pred", UUID.randomUUID(),
                List.of(), List.of(), UUID.randomUUID(), null, "GENERAL", 2.0, ClaimStatus.SUPPORTED, ValidationStatus.UNVALIDATED, projectId, 1
        );

        ScientificClaim result = claimValidationService.validateClaim(claim, UUID.randomUUID(), UUID.randomUUID(), userId);

        assertThat(result).isNotNull();
        assertThat(result.validationStatus()).isEqualTo(ValidationStatus.REJECTED);
    }

    @Test
    void testValidateClaim_Unauthorized() {
        shouldThrowAuth = true;
        ScientificClaim claim = new ScientificClaim(
                UUID.randomUUID(), "Valid statement", ClaimType.HYPOTHESIS, UUID.randomUUID(), "pred", UUID.randomUUID(),
                List.of(), List.of(), UUID.randomUUID(), null, "GENERAL", 1.0, ClaimStatus.SUPPORTED, ValidationStatus.UNVALIDATED, UUID.randomUUID(), 1
        );

        assertThatThrownBy(() -> claimValidationService.validateClaim(claim, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(GsirException.class)
                .hasMessageContaining("permission");
    }
}
