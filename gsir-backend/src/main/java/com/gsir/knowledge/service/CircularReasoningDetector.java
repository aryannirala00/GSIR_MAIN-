package com.gsir.knowledge.service;

import com.gsir.knowledge.adapter.GraphStorageAdapter;
import com.gsir.knowledge.model.KnowledgeRelationship;
import com.gsir.projects.ProjectAccessService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CircularReasoningDetector {

    private final GraphStorageAdapter graphAdapter;
    private final ProjectAccessService projectAccessService;

    public CircularReasoningDetector(GraphStorageAdapter graphAdapter, ProjectAccessService projectAccessService) {
        this.graphAdapter = graphAdapter;
        this.projectAccessService = projectAccessService;
    }

    /**
     * Fulfills Part 75.60: Circular Reasoning Detection.
     * Detects if there is a cycle starting from the given entity ID.
     */
    public boolean detectsCycle(UUID startEntityId, UUID projectId, UUID userId, int maxDepth) {
        projectAccessService.requireAccess(projectId, userId);
        Set<UUID> visited = new HashSet<>();
        Set<UUID> recursionStack = new HashSet<>();
        return hasCycle(startEntityId, projectId, maxDepth, 0, visited, recursionStack);
    }

    private boolean hasCycle(UUID currentId, UUID projectId, int maxDepth, int currentDepth, Set<UUID> visited, Set<UUID> recursionStack) {
        if (recursionStack.contains(currentId)) {
            return true; // Cycle detected
        }
        if (visited.contains(currentId) || currentDepth >= maxDepth) {
            return false;
        }

        visited.add(currentId);
        recursionStack.add(currentId);

        List<KnowledgeRelationship> edges = graphAdapter.retrieveRelationships(currentId, projectId);
        for (KnowledgeRelationship edge : edges) {
            if (hasCycle(edge.targetEntityId(), projectId, maxDepth, currentDepth + 1, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(currentId);
        return false;
    }
}
