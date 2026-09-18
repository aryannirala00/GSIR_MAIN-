package com.gsir.knowledge.extraction;

import com.gsir.knowledge.model.KnowledgeEntity;
import com.gsir.knowledge.model.KnowledgeRelationship;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.model.EvidenceLink;
import com.gsir.knowledge.model.ProvenanceRecord;
import java.util.List;
import java.util.UUID;

public interface KnowledgeExtractionPipeline {
    ExtractionResult extractFromDocument(UUID documentId, String textContent, UUID projectId);
    
    List<KnowledgeEntity> extractEntities(String textContent, UUID projectId);
    KnowledgeEntity resolveEntities(KnowledgeEntity entity, UUID projectId);
    List<KnowledgeRelationship> extractRelationships(String textContent, List<KnowledgeEntity> resolvedEntities, UUID projectId);
    List<EvidenceLink> linkEvidence(ScientificClaim claim, UUID documentId, UUID projectId);
    ProvenanceRecord attachProvenance(ScientificClaim claim, UUID sourceId, String extractorId, UUID projectId);
}

record ExtractionResult(
    List<KnowledgeEntity> entities,
    List<KnowledgeRelationship> relationships,
    List<ScientificClaim> claims
) {}
