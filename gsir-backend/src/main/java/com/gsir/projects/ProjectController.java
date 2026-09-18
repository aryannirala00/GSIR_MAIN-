package com.gsir.projects;

import com.gsir.common.api.ApiResponse;
import com.gsir.common.api.PageResponse;
import com.gsir.common.error.CommonErrors;
import com.gsir.projects.dto.CreateProjectRequest;
import com.gsir.projects.dto.ProjectResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectCommandService projectCommandService;
    private final ProjectRepository projectRepository;

    public ProjectController(ProjectCommandService projectCommandService, ProjectRepository projectRepository) {
        this.projectCommandService = projectCommandService;
        this.projectRepository = projectRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectResponse> createProject(
            @AuthenticationPrincipal UUID userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateProjectRequest request) {
        CreateProjectCommand command = new CreateProjectCommand(
                request.workspaceId(),
                request.name(),
                request.description()
        );
        ProjectEntity project = projectCommandService.createProject(userId, idempotencyKey, command);
        return ApiResponse.ok(ProjectResponse.from(project));
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponse> getProject(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID projectId) {
        ProjectEntity project = projectRepository.findAccessibleProject(projectId, userId)
                .orElseThrow(() -> CommonErrors.notFound("Project"));
        return ApiResponse.ok(ProjectResponse.from(project));
    }

    @GetMapping
    public ApiResponse<PageResponse<ProjectResponse>> listProjects(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ProjectResponse> projects = projectRepository.findAccessibleProjects(userId, workspaceId)
                .stream()
                .map(ProjectResponse::from)
                .toList();
        return ApiResponse.ok(PageResponse.of(projects, page, size));
    }
}
