package com.gsir.knowledge.service;

import com.gsir.knowledge.model.*;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.Map;

public interface KnowledgeService {
    KnowledgeEntity createEntity(String type, String canonicalName, List<String> aliases, Map<String, Object> properties, String domain, UUID projectId, UUID userId);
    Optional<KnowledgeEntity> getEntity(UUID entityId, UUID projectId, UUID userId);
    KnowledgeEntity updateEntity(KnowledgeEntity entity, UUID projectId, UUID userId);
    
    KnowledgeRelationship createRelationship(UUID source, UUID target, String type, Map<String, Object> properties, Double confidence, UUID provenanceId, UUID projectId, UUID userId);
    List<KnowledgeRelationship> getRelationships(UUID entityId, UUID projectId, UUID userId);
    
    ScientificClaim createClaim(String statement, ClaimType type, UUID subject, String predicate, UUID object, UUID provenanceId, UUID projectId, UUID userId);
    ScientificClaim updateClaimStatus(UUID claimId, ClaimStatus status, UUID projectId, UUID userId);
    ScientificClaim updateClaimValidationStatus(UUID claimId, com.gsir.knowledge.model.ValidationStatus status, UUID projectId, UUID userId);
    void attachEvidence(UUID claimId, EvidenceLink evidence, UUID projectId, UUID userId);
    List<ScientificClaim> findClaimsBySubjectAndPredicate(UUID subject, String predicate, UUID projectId, UUID userId);
    
    Optional<KnowledgeEntity> resolveEntity(String name, UUID projectId, UUID userId);
    
    List<KnowledgeEntity> searchKnowledge(String query, UUID projectId, UUID userId);
    List<List<KnowledgeRelationship>> traverseGraph(UUID startEntity, UUID targetEntity, int maxHops, UUID projectId, UUID userId);
    
    List<ScientificClaim> runInference(UUID startEntityId, String ruleSet, UUID projectId, UUID userId);
    List<ScientificClaim> detectConflict(UUID entityId, UUID projectId, UUID userId);
    
    String createSnapshot(UUID projectId, UUID userId);
    String exportKnowledge(UUID projectId, UUID userId);
    
    void deprecateKnowledge(UUID objectId, UUID projectId, UUID userId);
    List<KnowledgeRelationship> searchNeighborhood(UUID entityId, int depth, UUID projectId, UUID userId);
}
