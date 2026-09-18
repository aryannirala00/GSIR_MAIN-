package com.gsir.knowledge.controller;

import com.gsir.common.api.ApiResponse;
import com.gsir.knowledge.model.KnowledgeEntity;
import com.gsir.knowledge.model.KnowledgeRelationship;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.service.KnowledgeService;
import com.gsir.knowledge.service.EntityResolutionService;
import com.gsir.knowledge.service.ContradictionDetectionService;
import com.gsir.knowledge.service.CircularReasoningDetector;
import com.gsir.knowledge.service.KnowledgeGraphQueryEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final EntityResolutionService entityResolutionService;
    private final ContradictionDetectionService contradictionService;
    private final CircularReasoningDetector circularDetector;
    private final KnowledgeGraphQueryEngine queryEngine;

    public KnowledgeController(KnowledgeService knowledgeService, 
                               EntityResolutionService entityResolutionService,
                               ContradictionDetectionService contradictionService,
                               CircularReasoningDetector circularDetector,
                               KnowledgeGraphQueryEngine queryEngine) {
        this.knowledgeService = knowledgeService;
        this.entityResolutionService = entityResolutionService;
        this.contradictionService = contradictionService;
        this.circularDetector = circularDetector;
        this.queryEngine = queryEngine;
    }

    @PostMapping("/entities")
    public ResponseEntity<ApiResponse<KnowledgeEntity>> createEntity(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody EntityCreateRequest request) {
        KnowledgeEntity entity = knowledgeService.createEntity(
            request.type(), request.canonicalName(), request.aliases(), request.properties(), request.domain(), projectId, userId
        );
        return ResponseEntity.ok(ApiResponse.ok(entity));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KnowledgeEntity>>> searchKnowledge(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @RequestParam String query) {
        List<KnowledgeEntity> results = knowledgeService.searchKnowledge(query, projectId, userId);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @PostMapping("/entities/merge")
    public ResponseEntity<ApiResponse<KnowledgeEntity>> mergeEntities(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody MergeRequest request) {
        KnowledgeEntity merged = entityResolutionService.mergeEntities(projectId, userId, request.targetId(), request.sourceId());
        return ResponseEntity.ok(ApiResponse.ok(merged));
    }

    @PostMapping("/claims/contradictions")
    public ResponseEntity<ApiResponse<List<ScientificClaim>>> findContradictions(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody ScientificClaim claim) {
        return ResponseEntity.ok(ApiResponse.ok(contradictionService.findContradictions(claim, projectId, userId)));
    }

    @GetMapping("/entities/{entityId}/cycles")
    public ResponseEntity<ApiResponse<Boolean>> checkCycles(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID entityId,
            @RequestParam(defaultValue = "5") int maxDepth) {
        return ResponseEntity.ok(ApiResponse.ok(circularDetector.detectsCycle(entityId, projectId, userId, maxDepth)));
    }

    @GetMapping("/paths")
    public ResponseEntity<ApiResponse<List<List<KnowledgeRelationship>>>> findPaths(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @RequestParam UUID startEntityId,
            @RequestParam UUID endEntityId,
            @RequestParam(defaultValue = "3") int maxHops) {
        return ResponseEntity.ok(ApiResponse.ok(queryEngine.findPaths(startEntityId, endEntityId, maxHops, projectId, userId)));
    }
}

record EntityCreateRequest(
    String type,
    String canonicalName,
    List<String> aliases,
    Map<String, Object> properties,
    String domain
) {}

record MergeRequest(UUID targetId, UUID sourceId) {}
