package com.gsir.metadata;

import com.gsir.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/metadata")
public class MetadataController {

    private final ResearchObjectCatalogService catalogService;

    public MetadataController(ResearchObjectCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResearchObjectEntity>> createMetadata(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody CreateMetadataRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.createMetadata(projectId, userId, request)));
    }

    @GetMapping("/{objectId}")
    public ResponseEntity<ApiResponse<ResearchObjectEntity>> getMetadata(
            @PathVariable UUID projectId,
            @PathVariable UUID objectId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getMetadata(projectId, objectId, userId)));
    }

    @PutMapping("/{objectId}")
    public ResponseEntity<ApiResponse<ResearchObjectEntity>> updateMetadata(
            @PathVariable UUID projectId,
            @PathVariable UUID objectId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody UpdateMetadataRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.updateMetadata(projectId, objectId, userId, request.metadata(), request.versionId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResearchObjectEntity>>> listMetadata(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.listProjectObjects(projectId, userId)));
    }
}
