package com.gsir.workflow.goal;

import com.gsir.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Goal>> getGoal(
            @PathVariable UUID projectId,
            @PathVariable UUID goalId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.getGoal(goalId, projectId, userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Goal>> establishGoal(
            @PathVariable UUID projectId,
            @RequestBody CreateGoalRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                goalService.establishGoal(projectId, userId, request.description(), request.priority())
        ));
    }

    public record CreateGoalRequest(String description, double priority) {}
}
