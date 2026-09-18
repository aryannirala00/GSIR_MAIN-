package com.gsir.execution.controller;

import com.gsir.projects.ProjectAccessService;
import com.gsir.execution.model.ExecutionCostEntity;
import com.gsir.execution.model.HumanReviewTaskEntity;
import com.gsir.execution.service.CostTrackingService;
import com.gsir.execution.service.HumanReviewService;
import com.gsir.execution.service.SecretManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/execution")
public class ExecutionFeaturesController {

    private final CostTrackingService costService;
    private final HumanReviewService reviewService;
    private final SecretManagementService secretService;
    private final ProjectAccessService projectAccessService;

    public ExecutionFeaturesController(CostTrackingService costService, HumanReviewService reviewService, SecretManagementService secretService, ProjectAccessService projectAccessService) {
        this.costService = costService;
        this.reviewService = reviewService;
        this.secretService = secretService;
        this.projectAccessService = projectAccessService;
    }

    public record StoreSecretRequest(@NotBlank String secretName, @NotBlank String value) {}
    public record SecretResponse(UUID id, String secretName) {}

    @PostMapping("/secrets")
    public ResponseEntity<Void> storeSecret(@PathVariable UUID projectId, @AuthenticationPrincipal UUID userId, @Valid @RequestBody StoreSecretRequest req) {
        projectAccessService.requireAccess(projectId, userId);
        secretService.storeSecret(projectId, req.secretName(), req.value());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/secrets")
    public ResponseEntity<List<SecretResponse>> listSecrets(@PathVariable UUID projectId, @AuthenticationPrincipal UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        List<SecretResponse> secrets = secretService.listSecrets(projectId).stream()
            .map(s -> new SecretResponse(s.getId(), s.getSecretName()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(secrets);
    }

    @GetMapping("/costs")
    public ResponseEntity<List<ExecutionCostEntity>> getCosts(@PathVariable UUID projectId, @AuthenticationPrincipal UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        return ResponseEntity.ok(costService.getProjectCosts(projectId));
    }

    public record ResolveReviewRequest(@NotNull Map<String, Object> decision) {}

    @GetMapping("/reviews")
    public ResponseEntity<List<HumanReviewTaskEntity>> getReviews(@PathVariable UUID projectId, @AuthenticationPrincipal UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        return ResponseEntity.ok(reviewService.getProjectTasks(projectId));
    }

    @PostMapping("/reviews/{taskId}/resolve")
    public ResponseEntity<HumanReviewTaskEntity> resolveReview(@PathVariable UUID projectId, @PathVariable UUID taskId, @AuthenticationPrincipal UUID userId, @Valid @RequestBody ResolveReviewRequest req) {
        projectAccessService.requireAccess(projectId, userId);
        return ResponseEntity.ok(reviewService.resolveTask(projectId, taskId, userId, req.decision()));
    }
}


