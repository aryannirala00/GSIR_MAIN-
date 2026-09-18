package com.gsir.data.controller;

import com.gsir.common.api.ApiResponse;
import com.gsir.projects.ProjectAccessService;
import com.gsir.data.dto.RegisterDataRequest;
import com.gsir.data.model.DataObjectEntity;
import com.gsir.data.service.DataFabricService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/data")
public class DataFabricController {

    private final DataFabricService dataFabricService;
    private final ProjectAccessService projectAccessService;

    public DataFabricController(DataFabricService dataFabricService, ProjectAccessService projectAccessService) {
        this.dataFabricService = dataFabricService;
        this.projectAccessService = projectAccessService;
    }

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DataObjectEntity>> registerData(
            @PathVariable UUID projectId,
            @RequestBody RegisterDataRequest request) {
        
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        
        DataObjectEntity created = dataFabricService.registerData(projectId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DataObjectEntity>>> listData(
            @PathVariable UUID projectId,
            Pageable pageable) {
        
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        Page<DataObjectEntity> data = dataFabricService.listData(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{dataId}")
    public ResponseEntity<ApiResponse<DataObjectEntity>> getData(
            @PathVariable UUID projectId,
            @PathVariable UUID dataId) {
        
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        DataObjectEntity entity = dataFabricService.getData(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(entity));
    }
    
    @PutMapping("/{dataId}")
    public ResponseEntity<ApiResponse<DataObjectEntity>> updateDataset(
            @PathVariable UUID projectId,
            @PathVariable UUID dataId,
            @RequestBody RegisterDataRequest request) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        DataObjectEntity updated = dataFabricService.updateDataset(projectId, dataId, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{dataId}")
    public ResponseEntity<ApiResponse<Void>> deleteDataset(
            @PathVariable UUID projectId,
            @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.deleteDataset(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/archive")
    public ResponseEntity<ApiResponse<DataObjectEntity>> archiveDataset(
            @PathVariable UUID projectId,
            @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        DataObjectEntity archived = dataFabricService.archiveDataset(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(archived));
    }

    @PostMapping("/{dataId}/restore")
    public ResponseEntity<ApiResponse<DataObjectEntity>> restoreDataset(
            @PathVariable UUID projectId,
            @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        DataObjectEntity restored = dataFabricService.restoreDataset(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(restored));
    }

    @PostMapping("/{dataId}/upload")
    public ResponseEntity<ApiResponse<Void>> uploadData(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.uploadData(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/validate")
    public ResponseEntity<ApiResponse<Void>> validateData(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.validateData(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/profile")
    public ResponseEntity<ApiResponse<Void>> profileData(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.profileData(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/versions")
    public ResponseEntity<ApiResponse<Void>> createDatasetVersion(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.createDatasetVersion(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{dataId}/versions/{version}")
    public ResponseEntity<ApiResponse<Void>> getDatasetVersion(@PathVariable UUID projectId, @PathVariable UUID dataId, @PathVariable String version) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.getDatasetVersion(projectId, dataId, version);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/transform")
    public ResponseEntity<ApiResponse<Void>> transformDataset(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.transformDataset(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{dataId}/lineage")
    public ResponseEntity<ApiResponse<Void>> getLineage(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.getLineage(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{dataId}/dependencies")
    public ResponseEntity<ApiResponse<Void>> getDependencies(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.getDependencies(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/share")
    public ResponseEntity<ApiResponse<Void>> shareDataset(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.shareDataset(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/export")
    public ResponseEntity<ApiResponse<Void>> exportDataset(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.exportDataset(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{dataId}/quality")
    public ResponseEntity<ApiResponse<Void>> getQuality(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.getQuality(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{dataId}/metadata")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getMetadata(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        return ResponseEntity.ok(ApiResponse.ok(dataFabricService.getMetadata(projectId, dataId)));
    }

    @PutMapping("/{dataId}/metadata")
    public ResponseEntity<ApiResponse<DataObjectEntity>> updateMetadata(@PathVariable UUID projectId, @PathVariable UUID dataId, @RequestBody java.util.Map<String, Object> metadata) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        return ResponseEntity.ok(ApiResponse.ok(dataFabricService.updateMetadata(projectId, dataId, metadata)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Void>> searchDatasets(@PathVariable UUID projectId, @RequestParam String query) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.searchDatasets(projectId, query);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{dataId}/provenance")
    public ResponseEntity<ApiResponse<Void>> getProvenance(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.getProvenance(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/access/request")
    public ResponseEntity<ApiResponse<Void>> requestAccess(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.requestAccess(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/access/grant")
    public ResponseEntity<ApiResponse<Void>> grantAccess(@PathVariable UUID projectId, @PathVariable UUID dataId, @RequestParam UUID targetUserId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.grantAccess(projectId, dataId, targetUserId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/access/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeAccess(@PathVariable UUID projectId, @PathVariable UUID dataId, @RequestParam UUID targetUserId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.revokeAccess(projectId, dataId, targetUserId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{dataId}/audit")
    public ResponseEntity<ApiResponse<Void>> getAuditLog(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.getAuditLog(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{dataId}/mask")
    public ResponseEntity<ApiResponse<Void>> maskDataset(@PathVariable UUID projectId, @PathVariable UUID dataId) {
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        dataFabricService.maskDataset(projectId, dataId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{dataId}/classification")
    public ResponseEntity<ApiResponse<DataObjectEntity>> classifyData(
            @PathVariable UUID projectId,
            @PathVariable UUID dataId,
            @RequestParam String classification) {
        
        UUID userId = getCurrentUserId();
        projectAccessService.requireAccess(projectId, userId);
        DataObjectEntity updated = dataFabricService.classifyData(projectId, dataId, classification);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }
}
