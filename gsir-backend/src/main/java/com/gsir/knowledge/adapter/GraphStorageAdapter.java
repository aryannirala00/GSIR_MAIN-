package com.gsir.knowledge.adapter;

import com.gsir.knowledge.model.KnowledgeEntity;
import com.gsir.knowledge.model.KnowledgeRelationship;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.model.ProvenanceRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GraphStorageAdapter {
    KnowledgeEntity storeEntity(KnowledgeEntity entity);
    KnowledgeRelationship storeRelationship(KnowledgeRelationship relationship);
    ScientificClaim storeClaim(ScientificClaim claim);
    
    Optional<KnowledgeEntity> retrieveEntity(UUID entityId, UUID projectId);
    List<KnowledgeRelationship> retrieveRelationships(UUID sourceEntityId, UUID projectId);
    
    List<KnowledgeEntity> queryGraph(String query, UUID projectId);
    
    KnowledgeEntity updateKnowledge(KnowledgeEntity entity);
    ScientificClaim updateClaim(ScientificClaim claim);
    
    Optional<ProvenanceRecord> getProvenance(UUID provenanceId, UUID projectId);
    void storeProvenance(ProvenanceRecord provenanceRecord);
    
    List<List<KnowledgeRelationship>> findPaths(UUID startEntityId, UUID endEntityId, int maxHops, UUID projectId);
    java.util.List<com.gsir.knowledge.model.ScientificClaim> findClaimsBySubjectAndObject(java.util.UUID subjectId, java.util.UUID objectId, java.util.UUID projectId);
    java.util.List<com.gsir.knowledge.model.ScientificClaim> findClaimsBySubjectAndPredicate(java.util.UUID subjectId, String predicate, java.util.UUID projectId);
    java.util.List<com.gsir.knowledge.model.KnowledgeRelationship> retrieveRelationshipsByTarget(java.util.UUID targetEntityId, java.util.UUID projectId);
}


