package com.gsir.knowledge.adapter;

import com.gsir.knowledge.model.KnowledgeEntity;
import com.gsir.knowledge.model.KnowledgeRelationship;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.model.ProvenanceRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile("test")
public class DummyGraphAdapter implements GraphStorageAdapter {

    @Override
    public KnowledgeEntity storeEntity(KnowledgeEntity entity) {
        return entity;
    }

    @Override
    public KnowledgeRelationship storeRelationship(KnowledgeRelationship relationship) {
        return relationship;
    }

    @Override
    public ScientificClaim storeClaim(ScientificClaim claim) {
        return claim;
    }

    @Override
    public void storeProvenance(ProvenanceRecord provenanceRecord) {
    }

    @Override
    public Optional<KnowledgeEntity> retrieveEntity(UUID entityId, UUID projectId) {
        return Optional.empty();
    }

    @Override
    public List<KnowledgeRelationship> retrieveRelationships(UUID sourceEntityId, UUID projectId) {
        return Collections.emptyList();
    }

    @Override
    public List<KnowledgeEntity> queryGraph(String query, UUID projectId) {
        return Collections.emptyList();
    }

    @Override
    public KnowledgeEntity updateKnowledge(KnowledgeEntity entity) {
        return entity;
    }

    @Override
    public ScientificClaim updateClaim(ScientificClaim claim) {
        return claim;
    }

    @Override
    public Optional<ProvenanceRecord> getProvenance(UUID provenanceId, UUID projectId) {
        return Optional.empty();
    }

    @Override
    public List<List<KnowledgeRelationship>> findPaths(UUID startEntityId, UUID endEntityId, int maxHops, UUID projectId) {
        return Collections.emptyList();
    }

    @Override
    public List<ScientificClaim> findClaimsBySubjectAndObject(UUID subjectId, UUID objectId, UUID projectId) {
        return Collections.emptyList();
    }

    @Override
    public List<ScientificClaim> findClaimsBySubjectAndPredicate(UUID subjectId, String predicate, UUID projectId) {
        return Collections.emptyList();
    }

    @Override
    public List<KnowledgeRelationship> retrieveRelationshipsByTarget(UUID targetEntityId, UUID projectId) {
        return Collections.emptyList();
    }
}
