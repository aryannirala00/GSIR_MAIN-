package com.gsir.projects;

import java.util.UUID;

/**
 * Command object carrying the parameters needed to create a new Project.
 * Immutable value object — validated before being passed to the service layer.
 */
public record CreateProjectCommand(
        UUID workspaceId,
        String name,
        String description
) {}
