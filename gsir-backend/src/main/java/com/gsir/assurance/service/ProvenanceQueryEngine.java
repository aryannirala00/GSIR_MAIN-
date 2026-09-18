package com.gsir.assurance.service;

import com.gsir.knowledge.adapter.GraphStorageAdapter;
import com.gsir.knowledge.model.KnowledgeRelationship;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Service
public class ProvenanceQueryEngine {

    private final GraphStorageAdapter graphAdapter;

    public ProvenanceQueryEngine(GraphStorageAdapter graphAdapter) {
        this.graphAdapter = graphAdapter;
    }

    public List<KnowledgeRelationship> getBackwardProvenance(UUID targetEntityId, UUID projectId, int maxDepth) {
        List<KnowledgeRelationship> results = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        collectBackward(targetEntityId, projectId, maxDepth, 0, visited, results);
        return results;
    }

    private void collectBackward(UUID currentId, UUID projectId, int maxDepth, int currentDepth, Set<UUID> visited, List<KnowledgeRelationship> results) {
        if (currentDepth >= maxDepth || visited.contains(currentId)) {
            return;
        }
        visited.add(currentId);

        List<KnowledgeRelationship> edges = graphAdapter.retrieveRelationshipsByTarget(currentId, projectId);
        for (KnowledgeRelationship edge : edges) {
            results.add(edge);
            collectBackward(edge.sourceEntityId(), projectId, maxDepth, currentDepth + 1, visited, results);
        }
    }

    public List<KnowledgeRelationship> getForwardProvenance(UUID sourceEntityId, UUID projectId, int maxDepth) {
        List<KnowledgeRelationship> results = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        collectForward(sourceEntityId, projectId, maxDepth, 0, visited, results);
        return results;
    }

    private void collectForward(UUID currentId, UUID projectId, int maxDepth, int currentDepth, Set<UUID> visited, List<KnowledgeRelationship> results) {
        if (currentDepth >= maxDepth || visited.contains(currentId)) {
            return;
        }
        visited.add(currentId);

        List<KnowledgeRelationship> edges = graphAdapter.retrieveRelationships(currentId, projectId);
        for (KnowledgeRelationship edge : edges) {
            results.add(edge);
            collectForward(edge.targetEntityId(), projectId, maxDepth, currentDepth + 1, visited, results);
        }
    }
}
