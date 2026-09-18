package com.gsir.ai.research;

import com.gsir.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/research-loop")
public class ScientificResearchLoopController {

    private final ScientificResearchLoop researchLoop;

    public ScientificResearchLoopController(ScientificResearchLoop researchLoop) {
        this.researchLoop = researchLoop;
    }

    public record ExecuteLoopRequest(String researchQuestion, int maxIterations) {}

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<ResearchLoopState>> execute(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody ExecuteLoopRequest request) {
        
        ResearchLoopState state = researchLoop.executeLoop(
                projectId, 
                userId, 
                request.researchQuestion(), 
                request.maxIterations() > 0 ? request.maxIterations() : 5
        );
        
        return ResponseEntity.ok(ApiResponse.ok(state));
    }
}
