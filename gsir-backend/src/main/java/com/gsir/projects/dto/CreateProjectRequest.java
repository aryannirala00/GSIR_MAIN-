package com.gsir.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProjectRequest(
        @NotNull(message = "workspaceId must not be null")
        UUID workspaceId,

        @NotBlank(message = "Project name must not be blank")
        String name,

        String description
) {}
