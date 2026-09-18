package com.gsir.workflow;

import com.gsir.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/workflows")
public class WorkflowController {

    private final WorkflowOrchestrator orchestrator;

    public WorkflowController(WorkflowOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkflowResponse>> createWorkflow(
            @PathVariable UUID projectId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateWorkflowRequest request,
            @AuthenticationPrincipal UUID userId) {
            
        ResearchWorkflowEntity workflow = orchestrator.createWorkflow(projectId, userId, idempotencyKey, request);
        return ResponseEntity.ok(ApiResponse.ok(WorkflowResponse.fromEntity(workflow)));
    }
    
    @PostMapping("/{workflowId}/start")
    public ResponseEntity<ApiResponse<Void>> startWorkflow(
            @PathVariable UUID projectId,
            @PathVariable UUID workflowId,
            @AuthenticationPrincipal UUID userId) {
            
        orchestrator.startWorkflow(workflowId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{workflowId}/pause")
    public ResponseEntity<ApiResponse<Void>> pauseWorkflow(
            @PathVariable UUID projectId,
            @PathVariable UUID workflowId,
            @AuthenticationPrincipal UUID userId) {
            
        orchestrator.pauseWorkflow(workflowId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{workflowId}/resume")
    public ResponseEntity<ApiResponse<Void>> resumeWorkflow(
            @PathVariable UUID projectId,
            @PathVariable UUID workflowId,
            @AuthenticationPrincipal UUID userId) {
            
        orchestrator.resumeWorkflow(workflowId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{workflowId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelWorkflow(
            @PathVariable UUID projectId,
            @PathVariable UUID workflowId,
            @AuthenticationPrincipal UUID userId) {
            
        orchestrator.cancelWorkflow(workflowId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
