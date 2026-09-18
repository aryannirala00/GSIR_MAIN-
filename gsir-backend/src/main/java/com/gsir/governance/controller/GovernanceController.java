package com.gsir.governance.controller;

import com.gsir.common.api.ApiResponse;
import com.gsir.governance.model.AuditLogEntity;
import com.gsir.governance.service.AuditLogService;
import com.gsir.projects.ProjectAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/governance")
public class GovernanceController {

    private final AuditLogService auditLogService;
    private final ProjectAccessService projectAccessService;

    public GovernanceController(AuditLogService auditLogService, ProjectAccessService projectAccessService) {
        this.auditLogService = auditLogService;
        this.projectAccessService = projectAccessService;
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogEntity>>> getAuditLogs(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId) {
        
        projectAccessService.requireAccess(projectId, userId);
        List<AuditLogEntity> logs = auditLogService.getProjectAuditLogs(projectId);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
