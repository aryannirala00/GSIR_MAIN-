package com.gsir.provenance.service;

import com.gsir.provenance.model.*;
import com.gsir.provenance.repository.ProvenanceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProvenanceService {

    private final ProvenanceRepository repository;

    public ProvenanceService(ProvenanceRepository repository) {
        this.repository = repository;
    }

    public ProvAgent registerAgent(AgentType type, String name, Map<String, Object> metadata, UUID projectId) {
        return repository.saveAgent(new ProvAgent(UUID.randomUUID(), projectId, type, name, metadata, Instant.now()));
    }

    public ProvActivity startActivity(String activityType, Map<String, Object> parameters, Map<String, Object> env, UUID projectId) {
        return repository.saveActivity(new ProvActivity(UUID.randomUUID(), projectId, activityType, "RUNNING", Instant.now(), null, parameters, env, Instant.now()));
    }

    public ProvArtifact registerArtifact(String artifactType, String hash, int version, String uri, Map<String, Object> metadata, UUID projectId) {
        return repository.saveArtifact(new ProvArtifact(UUID.randomUUID(), projectId, artifactType, hash, version, uri, metadata, Instant.now()));
    }

    public void linkUsed(UUID activityId, UUID artifactId, UUID projectId) {
        repository.saveRelationship(new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.USED, activityId, artifactId, Instant.now()));
    }

    public void linkGenerated(UUID artifactId, UUID activityId, UUID projectId) {
        repository.saveRelationship(new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_GENERATED_BY, artifactId, activityId, Instant.now()));
    }
    
    public void linkDerived(UUID generatedArtifactId, UUID sourceArtifactId, UUID projectId) {
        repository.saveRelationship(new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_DERIVED_FROM, generatedArtifactId, sourceArtifactId, Instant.now()));
    }

    public List<ProvRelationship> getForwardLineage(UUID sourceId, UUID projectId) {
        return repository.findForwardLineage(sourceId, projectId);
    }

    public List<ProvRelationship> getBackwardLineage(UUID targetId, UUID projectId) {
        return repository.findBackwardLineage(targetId, projectId);
    }

    public Map<String, Object> exportProvenance(UUID projectId) {
        return Map.of(
                "projectId", projectId,
                "artifacts", repository.findArtifactsByProjectId(projectId),
                "activities", repository.findActivitiesByProjectId(projectId),
                "agents", repository.findAgentsByProjectId(projectId),
                "relationships", repository.findRelationshipsByProjectId(projectId)
        );
    }

    public boolean validateProvenance(UUID projectId) {
        List<ProvRelationship> relationships = repository.findRelationshipsByProjectId(projectId);
        for (ProvRelationship rel : relationships) {
            boolean sourceExists = repository.artifactExists(rel.sourceId(), projectId) || 
                                   repository.activityExists(rel.sourceId(), projectId) ||
                                   repository.agentExists(rel.sourceId(), projectId);
            if (!sourceExists) return false;

            boolean targetExists = repository.artifactExists(rel.targetId(), projectId) || 
                                   repository.activityExists(rel.targetId(), projectId) ||
                                   repository.agentExists(rel.targetId(), projectId);
            if (!targetExists) return false;
        }
        return true;
    }

    public List<ProvRelationship> getImpactAnalysis(UUID sourceId, UUID projectId, int maxDepth) {
        java.util.List<ProvRelationship> impact = new java.util.ArrayList<>();
        java.util.Set<UUID> visited = new java.util.HashSet<>();
        java.util.Queue<UUID> queue = new java.util.LinkedList<>();
        
        queue.add(sourceId);
        visited.add(sourceId);
        
        int currentDepth = 0;
        
        while (!queue.isEmpty() && currentDepth < maxDepth) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                UUID current = queue.poll();
                List<ProvRelationship> forward = repository.findForwardLineage(current, projectId);
                for (ProvRelationship rel : forward) {
                    if (!impact.contains(rel)) {
                        impact.add(rel);
                    }
                    if (!visited.contains(rel.targetId())) {
                        visited.add(rel.targetId());
                        queue.add(rel.targetId());
                    }
                }
            }
            currentDepth++;
        }
        
        // Ensure deterministic output
        impact.sort(java.util.Comparator.comparing(ProvRelationship::id));
        return impact;
    }

    public List<ProvRelationship> getBackwardLineageRecursive(UUID targetId, UUID projectId, int maxDepth) {
        java.util.List<ProvRelationship> lineage = new java.util.ArrayList<>();
        java.util.Set<UUID> visited = new java.util.HashSet<>();
        java.util.Queue<UUID> queue = new java.util.LinkedList<>();
        
        queue.add(targetId);
        visited.add(targetId);
        
        int currentDepth = 0;
        
        while (!queue.isEmpty() && currentDepth < maxDepth) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                UUID current = queue.poll();
                List<ProvRelationship> backward = repository.findBackwardLineage(current, projectId);
                for (ProvRelationship rel : backward) {
                    if (!lineage.contains(rel)) {
                        lineage.add(rel);
                    }
                    if (!visited.contains(rel.sourceId())) {
                        visited.add(rel.sourceId());
                        queue.add(rel.sourceId());
                    }
                }
            }
            currentDepth++;
        }
        
        lineage.sort(java.util.Comparator.comparing(ProvRelationship::id));
        return lineage;
    }

    public List<ProvRelationship> getEvidenceChain(UUID claimId, UUID projectId, int maxDepth) {
        java.util.List<ProvRelationship> chain = new java.util.ArrayList<>();
        java.util.Set<UUID> visited = new java.util.HashSet<>();
        java.util.Queue<UUID> queue = new java.util.LinkedList<>();
        
        queue.add(claimId);
        visited.add(claimId);
        
        int currentDepth = 0;
        
        while (!queue.isEmpty() && currentDepth < maxDepth) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                UUID current = queue.poll();
                List<ProvRelationship> backward = repository.findBackwardLineage(current, projectId);
                for (ProvRelationship rel : backward) {
                    if (rel.relationshipType() == RelationshipType.SUPPORTED_BY || rel.relationshipType() == RelationshipType.CONTRADICTED_BY) {
                        if (!chain.contains(rel)) {
                            chain.add(rel);
                        }
                        if (!visited.contains(rel.sourceId())) {
                            visited.add(rel.sourceId());
                            queue.add(rel.sourceId());
                        }
                    }
                }
            }
            currentDepth++;
        }
        
        chain.sort(java.util.Comparator.comparing(ProvRelationship::id));
        return chain;
    }

    public List<ProvRelationship> getClaimChain(UUID evidenceId, UUID projectId, int maxDepth) {
        java.util.List<ProvRelationship> chain = new java.util.ArrayList<>();
        java.util.Set<UUID> visited = new java.util.HashSet<>();
        java.util.Queue<UUID> queue = new java.util.LinkedList<>();
        
        queue.add(evidenceId);
        visited.add(evidenceId);
        
        int currentDepth = 0;
        
        while (!queue.isEmpty() && currentDepth < maxDepth) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                UUID current = queue.poll();
                List<ProvRelationship> forward = repository.findForwardLineage(current, projectId);
                for (ProvRelationship rel : forward) {
                    if (rel.relationshipType() == RelationshipType.SUPPORTED_BY || rel.relationshipType() == RelationshipType.CONTRADICTED_BY) {
                        if (!chain.contains(rel)) {
                            chain.add(rel);
                        }
                        if (!visited.contains(rel.targetId())) {
                            visited.add(rel.targetId());
                            queue.add(rel.targetId());
                        }
                    }
                }
            }
            currentDepth++;
        }
        
        chain.sort(java.util.Comparator.comparing(ProvRelationship::id));
        return chain;
    }

    public Map<String, Object> compareProvenance(UUID projectId, UUID sourceId, UUID targetId) {
        List<ProvRelationship> sourceLineage = getBackwardLineageRecursive(sourceId, projectId, 100);
        List<ProvRelationship> targetLineage = getBackwardLineageRecursive(targetId, projectId, 100);
        
        java.util.Set<ProvRelationship> sourceSet = new java.util.HashSet<>(sourceLineage);
        java.util.Set<ProvRelationship> targetSet = new java.util.HashSet<>(targetLineage);
        
        java.util.Set<ProvRelationship> common = new java.util.HashSet<>(sourceSet);
        common.retainAll(targetSet);
        
        java.util.Set<ProvRelationship> sourceOnly = new java.util.HashSet<>(sourceSet);
        sourceOnly.removeAll(targetSet);
        
        java.util.Set<ProvRelationship> targetOnly = new java.util.HashSet<>(targetSet);
        targetOnly.removeAll(sourceSet);
        
        List<ProvRelationship> commonList = new java.util.ArrayList<>(common);
        commonList.sort(java.util.Comparator.comparing(ProvRelationship::id));
        List<ProvRelationship> sourceOnlyList = new java.util.ArrayList<>(sourceOnly);
        sourceOnlyList.sort(java.util.Comparator.comparing(ProvRelationship::id));
        List<ProvRelationship> targetOnlyList = new java.util.ArrayList<>(targetOnly);
        targetOnlyList.sort(java.util.Comparator.comparing(ProvRelationship::id));

        return Map.of(
                "projectId", projectId,
                "commonLineage", commonList,
                "sourceOnlyLineage", sourceOnlyList,
                "targetOnlyLineage", targetOnlyList,
                "isIdentical", sourceOnlyList.isEmpty() && targetOnlyList.isEmpty()
        );
    }

    public List<ProvRelationship> getEntityHistory(UUID entityId, UUID projectId) {
        List<ProvRelationship> history = new java.util.ArrayList<>();
        history.addAll(repository.findBackwardLineage(entityId, projectId));
        history.addAll(repository.findForwardLineage(entityId, projectId));
        return history;
    }

    public List<ProvRelationship> getVersionLineage(UUID entityId, UUID projectId) {
        return repository.findRelationshipsByProjectId(projectId).stream()
                .filter(r -> r.relationshipType() == RelationshipType.VERSION_OF && (r.sourceId().equals(entityId) || r.targetId().equals(entityId)))
                .toList();
    }

    public String checkCompleteness(UUID executionId, UUID projectId) {
        List<ProvRelationship> forward = repository.findForwardLineage(executionId, projectId);
        List<ProvRelationship> backward = repository.findBackwardLineage(executionId, projectId);
        if (forward.isEmpty() && backward.isEmpty()) return "INCOMPLETE";
        if (backward.isEmpty()) return "PARTIAL";
        return "COMPLETE";
    }

    public Map<String, Object> createSnapshot(UUID projectId) {
        Map<String, Object> snapshotData = exportProvenance(projectId);
        ProvArtifact snapshotArtifact = registerArtifact(
                "PROVENANCE_SNAPSHOT", 
                "hash-placeholder", 
                1, 
                "internal://snapshot", 
                Map.of("snapshot", "true"), 
                projectId
        );
        return Map.of(
                "snapshotId", snapshotArtifact.id(),
                "projectId", projectId,
                "timestamp", Instant.now().toString(),
                "data", snapshotData
        );
    }

    public Map<String, Object> generateProvenanceReport(UUID entityId, UUID projectId) {
        return Map.of(
                "entityId", entityId,
                "projectId", projectId,
                "backwardLineage", getBackwardLineageRecursive(entityId, projectId, 10),
                "forwardLineage", getImpactAnalysis(entityId, projectId, 10),
                "generatedAt", Instant.now().toString()
        );
    }

    public Map<String, Object> generateReproductionReport(UUID executionId, UUID projectId) {
        return Map.of(
                "executionId", executionId,
                "projectId", projectId,
                "requiredData", getBackwardLineageRecursive(executionId, projectId, 10),
                "requiredEnvironment", "Inherited from Execution Context",
                "completeness", checkCompleteness(executionId, projectId),
                "generatedAt", Instant.now().toString()
        );
    }

    public Map<String, Object> generateAuditReport(UUID entityId, UUID projectId) {
        return Map.of(
                "entityId", entityId,
                "projectId", projectId,
                "history", getEntityHistory(entityId, projectId),
                "validation", validateProvenance(projectId) ? "VERIFIED" : "UNVERIFIED",
                "generatedAt", Instant.now().toString()
        );
    }

    public Map<String, Object> importLineage(UUID projectId, Map<String, Object> importedData) {
        // Mocking import process by acknowledging receipt and tagging as IMPORTED
        return Map.of(
                "projectId", projectId,
                "status", "IMPORTED",
                "importedRecords", importedData.size(),
                "timestamp", Instant.now().toString()
        );
    }
}
