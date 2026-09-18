package com.gsir.notes;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record NoteResponse(
    UUID id,
    String title,
    String content,
    String summary,
    UUID projectId,
    UUID workspaceId,
    UUID organizationId,
    UUID userId,
    boolean isArchived,
    boolean aiGenerated,
    Integer versionNumber,
    Instant createdAt,
    Instant updatedAt,
    Set<String> tags
) {
    public static NoteResponse from(NoteEntity entity) {
        return new NoteResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getContent(),
            entity.getSummary(),
            entity.getProjectId(),
            entity.getWorkspaceId(),
            entity.getOrganizationId(),
            entity.getUserId(),
            entity.isArchived(),
            entity.isAiGenerated(),
            entity.getVersionNumber(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getTags()
        );
    }
}

