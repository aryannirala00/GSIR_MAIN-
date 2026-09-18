package com.gsir.knowledge.service;

import com.gsir.knowledge.adapter.GraphStorageAdapter;
import com.gsir.knowledge.model.KnowledgeRelationship;
import com.gsir.projects.ProjectAccessService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Service
public class KnowledgeGraphQueryEngine {

    private final GraphStorageAdapter graphAdapter;
    private final ProjectAccessService projectAccessService;

    public KnowledgeGraphQueryEngine(GraphStorageAdapter graphAdapter, ProjectAccessService projectAccessService) {
        this.graphAdapter = graphAdapter;
        this.projectAccessService = projectAccessService;
    }

    /**
     * Fulfills Part 75.53: Multi-hop reasoning.
     * Finds paths up to maxHops from startEntityId.
     */
    public List<List<KnowledgeRelationship>> findPaths(UUID startEntityId, UUID endEntityId, int maxHops, UUID projectId, UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        // We simulate a DFS up to maxHops to find paths
        List<List<KnowledgeRelationship>> allPaths = new ArrayList<>();
        List<KnowledgeRelationship> currentPath = new ArrayList<>();
        dfs(startEntityId, endEntityId, maxHops, 0, projectId, currentPath, allPaths);
        return allPaths;
    }

    private void dfs(UUID currentId, UUID endId, int maxHops, int currentHop, UUID projectId,
                     List<KnowledgeRelationship> currentPath, List<List<KnowledgeRelationship>> allPaths) {
        if (currentId.equals(endId) && !currentPath.isEmpty()) {
            allPaths.add(new ArrayList<>(currentPath));
            return;
        }
        if (currentHop >= maxHops) {
            return;
        }

        List<KnowledgeRelationship> edges = graphAdapter.retrieveRelationships(currentId, projectId);
        for (KnowledgeRelationship edge : edges) {
            // Prevent trivial cycles in path
            if (currentPath.stream().anyMatch(e -> e.targetEntityId().equals(edge.targetEntityId()))) {
                continue;
            }
            currentPath.add(edge);
            dfs(edge.targetEntityId(), endId, maxHops, currentHop + 1, projectId, currentPath, allPaths);
            currentPath.remove(currentPath.size() - 1);
        }
    }
}
