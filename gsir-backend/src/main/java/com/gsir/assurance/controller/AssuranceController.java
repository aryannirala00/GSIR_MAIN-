package com.gsir.assurance.controller;

import com.gsir.common.api.ApiResponse;
import com.gsir.assurance.model.ResearchArtifactEntity;
import com.gsir.assurance.model.AssuranceAttestationEntity;
import com.gsir.assurance.service.ArtifactAssuranceService;
import com.gsir.assurance.service.ProvenanceQueryEngine;
import com.gsir.knowledge.model.KnowledgeRelationship;
import com.gsir.projects.ProjectAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/assurance")
public class AssuranceController {

    private final ArtifactAssuranceService artifactAssuranceService;
    private final ProvenanceQueryEngine provenanceQueryEngine;
    private final ProjectAccessService projectAccessService;

    public AssuranceController(ArtifactAssuranceService artifactAssuranceService,
                               ProvenanceQueryEngine provenanceQueryEngine,
                               ProjectAccessService projectAccessService) {
        this.artifactAssuranceService = artifactAssuranceService;
        this.provenanceQueryEngine = provenanceQueryEngine;
        this.projectAccessService = projectAccessService;
    }

    @PostMapping("/artifacts")
    public ResponseEntity<ApiResponse<ResearchArtifactEntity>> registerArtifact(
            @PathVariable UUID projectId,
            @Valid @RequestBody RegisterArtifactRequest request,
            @AuthenticationPrincipal UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        ResearchArtifactEntity artifact = artifactAssuranceService.registerArtifact(
                projectId, userId, request.objectType(), request.locationUri(), request.expectedHash(), request.hashAlgorithm());
        return ResponseEntity.ok(ApiResponse.ok(artifact));
    }

    @PostMapping("/artifacts/{artifactId}/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyArtifact(
            @PathVariable UUID projectId,
            @PathVariable UUID artifactId,
            @Valid @RequestBody VerifyArtifactRequest request,
            @AuthenticationPrincipal UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        boolean isValid = artifactAssuranceService.verifyArtifactHash(projectId, artifactId, userId, request.actualHash());
        return ResponseEntity.ok(ApiResponse.ok(isValid));
    }

    @PostMapping("/artifacts/{artifactId}/attest")
    public ResponseEntity<ApiResponse<AssuranceAttestationEntity>> attestAssurance(
            @PathVariable UUID projectId,
            @PathVariable UUID artifactId,
            @Valid @RequestBody AttestRequest request,
            @AuthenticationPrincipal UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        AssuranceAttestationEntity attestation = artifactAssuranceService.attestAssurance(
                projectId, artifactId, userId, request.assuranceLevel(), request.score(), request.metadata());
        return ResponseEntity.ok(ApiResponse.ok(attestation));
    }

    @GetMapping("/provenance/backward/{entityId}")
    public ResponseEntity<ApiResponse<List<KnowledgeRelationship>>> getBackwardProvenance(
            @PathVariable UUID projectId,
            @PathVariable UUID entityId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int maxDepth,
            @AuthenticationPrincipal UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        List<KnowledgeRelationship> results = provenanceQueryEngine.getBackwardProvenance(entityId, projectId, maxDepth);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/provenance/forward/{entityId}")
    public ResponseEntity<ApiResponse<List<KnowledgeRelationship>>> getForwardProvenance(
            @PathVariable UUID projectId,
            @PathVariable UUID entityId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int maxDepth,
            @AuthenticationPrincipal UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        List<KnowledgeRelationship> results = provenanceQueryEngine.getForwardProvenance(entityId, projectId, maxDepth);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}

record RegisterArtifactRequest(
    @NotBlank String objectType, 
    @NotBlank String locationUri, 
    @NotBlank String expectedHash, 
    @NotBlank String hashAlgorithm
) {}

record VerifyArtifactRequest(
    @NotBlank String actualHash
) {}

record AttestRequest(
    @NotBlank String assuranceLevel, 
    @NotNull @Min(0) @Max(1) Double score, 
    Map<String, Object> metadata
) {}
