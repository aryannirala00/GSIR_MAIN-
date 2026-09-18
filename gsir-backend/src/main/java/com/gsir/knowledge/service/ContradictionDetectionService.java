package com.gsir.knowledge.service;

import com.gsir.knowledge.adapter.GraphStorageAdapter;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.projects.ProjectAccessService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContradictionDetectionService {

    private final GraphStorageAdapter graphAdapter;
    private final ProjectAccessService projectAccessService;

    public ContradictionDetectionService(GraphStorageAdapter graphAdapter, ProjectAccessService projectAccessService) {
        this.graphAdapter = graphAdapter;
        this.projectAccessService = projectAccessService;
    }

    /**
     * Fulfills Part 75.21 and 75.61: Contradictory Claims and Knowledge Conflict Detection.
     * Identifies claims that have the same subject and object but conflicting predicates.
     */
    public List<ScientificClaim> findContradictions(ScientificClaim baseClaim, UUID projectId, UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        List<ScientificClaim> relatedClaims = graphAdapter.findClaimsBySubjectAndObject(
                baseClaim.subjectEntityId(), baseClaim.objectEntityId(), projectId);

        return relatedClaims.stream()
                .filter(claim -> !claim.claimId().equals(baseClaim.claimId()))
                .filter(claim -> isContradictory(baseClaim.predicate(), claim.predicate()))
                .collect(Collectors.toList());
    }

    private boolean isContradictory(String pred1, String pred2) {
        if (pred1 == null || pred2 == null) return false;
        String p1 = pred1.toLowerCase();
        String p2 = pred2.toLowerCase();
        
        // Simple heuristic for scientific contradictions
        if (p1.equals("inhibits") && p2.equals("activates")) return true;
        if (p1.equals("activates") && p2.equals("inhibits")) return true;
        if (p1.equals("increases") && p2.equals("decreases")) return true;
        if (p1.equals("decreases") && p2.equals("increases")) return true;
        if (p1.equals("causes") && p2.equals("prevents")) return true;
        if (p1.equals("prevents") && p2.equals("causes")) return true;
        
        return false;
    }
}
