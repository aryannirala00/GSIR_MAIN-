package com.gsir.federation.controller;

import com.gsir.common.api.ApiResponse;
import com.gsir.federation.dto.RegisterSiteRequest;
import com.gsir.federation.dto.SubmitRemoteExecutionRequest;
import com.gsir.federation.model.FederationSiteEntity;
import com.gsir.federation.model.RemoteExecutionEntity;
import com.gsir.federation.service.FederationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/federation")
public class FederationController {

    private final FederationService federationService;

    public FederationController(FederationService federationService) {
        this.federationService = federationService;
    }

    @PostMapping("/sites")
    public ResponseEntity<ApiResponse<FederationSiteEntity>> registerSite(@RequestBody RegisterSiteRequest request) {
        FederationSiteEntity site = federationService.registerSite(request);
        return ResponseEntity.ok(ApiResponse.ok(site));
    }

    @GetMapping("/sites")
    public ResponseEntity<ApiResponse<List<FederationSiteEntity>>> listSites() {
        return ResponseEntity.ok(ApiResponse.ok(federationService.listSites()));
    }

    @GetMapping("/sites/{siteId}")
    public ResponseEntity<ApiResponse<FederationSiteEntity>> getSite(@PathVariable UUID siteId) {
        return ResponseEntity.ok(ApiResponse.ok(federationService.getSite(siteId)));
    }

    @PostMapping("/projects/{projectId}/sites/{siteId}/executions")
    public ResponseEntity<ApiResponse<RemoteExecutionEntity>> submitRemoteExecution(
            @PathVariable UUID projectId,
            @PathVariable UUID siteId,
            @RequestBody SubmitRemoteExecutionRequest request) {
        UUID userId = (UUID) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RemoteExecutionEntity execution = federationService.submitRemoteExecution(projectId, userId, siteId, request);
        return ResponseEntity.ok(ApiResponse.ok(execution));
    }

    @GetMapping("/projects/{projectId}/executions/{executionId}")
    public ResponseEntity<ApiResponse<RemoteExecutionEntity>> getRemoteExecution(
            @PathVariable UUID projectId,
            @PathVariable UUID executionId) {
        UUID userId = (UUID) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(federationService.getRemoteExecution(projectId, userId, executionId)));
    }
}
