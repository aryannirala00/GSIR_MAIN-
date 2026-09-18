package com.gsir.knowledge.service;

import com.gsir.knowledge.adapter.GraphStorageAdapter;
import com.gsir.knowledge.model.KnowledgeEntity;
import com.gsir.projects.ProjectAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Service
public class EntityResolutionService {

    private final GraphStorageAdapter graphAdapter;
    private final ProjectAccessService projectAccessService;

    public EntityResolutionService(GraphStorageAdapter graphAdapter, ProjectAccessService projectAccessService) {
        this.graphAdapter = graphAdapter;
        this.projectAccessService = projectAccessService;
    }

    /**
     * Merges sourceEntity into targetEntity by transferring aliases (Part 74.11).
     * The source entity is marked as merged/deprecated.
     */
    @Transactional
    public KnowledgeEntity mergeEntities(UUID projectId, UUID userId, UUID targetId, UUID sourceId) {
        projectAccessService.requireAccess(projectId, userId);
        
        KnowledgeEntity target = graphAdapter.retrieveEntity(targetId, projectId)
                .orElseThrow(() -> new RuntimeException("Target entity not found"));
        KnowledgeEntity source = graphAdapter.retrieveEntity(sourceId, projectId)
                .orElseThrow(() -> new RuntimeException("Source entity not found"));

        List<String> newAliases = new ArrayList<>(target.aliases());
        if (!newAliases.contains(source.canonicalName())) {
            newAliases.add(source.canonicalName());
        }
        newAliases.addAll(source.aliases());

        KnowledgeEntity updatedTarget = new KnowledgeEntity(
                target.entityId(), target.entityType(), target.canonicalName(),
                newAliases, target.description(), target.properties(),
                target.domain(), target.sourceReferences(), target.confidence(),
                target.validationStatus(), target.status(), target.projectId(),
                target.createdAt(), target.updatedAt(), target.version() + 1
        );
        
        // Mark source as deprecated/merged
        KnowledgeEntity updatedSource = new KnowledgeEntity(
                source.entityId(), source.entityType(), source.canonicalName(),
                source.aliases(), source.description(), source.properties(),
                source.domain(), source.sourceReferences(), source.confidence(),
                source.validationStatus(), "MERGED_INTO_" + targetId, source.projectId(),
                source.createdAt(), source.updatedAt(), source.version() + 1
        );
        
        graphAdapter.updateKnowledge(updatedSource);
        return graphAdapter.updateKnowledge(updatedTarget);
    }
}
