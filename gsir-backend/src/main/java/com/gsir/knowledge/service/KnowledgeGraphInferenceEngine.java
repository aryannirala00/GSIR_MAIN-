package com.gsir.knowledge.service;

import com.gsir.knowledge.adapter.GraphStorageAdapter;
import com.gsir.knowledge.model.KnowledgeRelationship;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;
import java.time.Instant;

@Service
public class KnowledgeGraphInferenceEngine {

    private final GraphStorageAdapter graphAdapter;

    public KnowledgeGraphInferenceEngine(GraphStorageAdapter graphAdapter) {
        this.graphAdapter = graphAdapter;
    }

    /**
     * Performs a single-hop transitive inference check:
     * If A -> B (type1) and B -> C (type2) then infer A -> C (inferredType).
     * This fulfills Part 74.44 (Inference Engine) generating derived relationships.
     */
    public KnowledgeRelationship inferTransitiveRelationship(
            UUID sourceId, 
            UUID intermediateId, 
            UUID targetId, 
            String inferredType, 
            UUID projectId) {
            
        List<KnowledgeRelationship> sourceToInter = graphAdapter.retrieveRelationships(sourceId, projectId);
        List<KnowledgeRelationship> interToTarget = graphAdapter.retrieveRelationships(intermediateId, projectId);
        
        boolean hasPath1 = sourceToInter.stream().anyMatch(r -> r.targetEntityId().equals(intermediateId));
        boolean hasPath2 = interToTarget.stream().anyMatch(r -> r.targetEntityId().equals(targetId));
        
        if (hasPath1 && hasPath2) {
            Map<String, Object> inferenceProv = Map.of(
                "rule", "TRANSITIVE_HOP",
                "intermediate_node", intermediateId.toString(),
                "timestamp", Instant.now().toString()
            );
            
            KnowledgeRelationship inferred = new KnowledgeRelationship(
                    UUID.randomUUID(),
                    inferredType,
                    sourceId,
                    targetId,
                    Collections.emptyMap(),
                    0.75, // Confidence degrades via inference
                    null,
                    Collections.emptyList(),
                    null,
                    com.gsir.knowledge.model.ValidationStatus.UNVALIDATED,
                    "ACTIVE",
                    projectId,
                    1,
                    true,
                    inferenceProv
            );
            
            return graphAdapter.storeRelationship(inferred);
        }
        return null;
    }
}
