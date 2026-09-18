package com.gsir.conversations.dto;

import com.gsir.conversations.ConversationEntity;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        UUID userId,
        UUID workspaceId,
        UUID projectId,
        String title,
        String mode,
        String status,
        String modelId,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt,
        String metadata
) {
    public static ConversationResponse fromEntity(ConversationEntity entity) {
        return new ConversationResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getWorkspaceId(),
                entity.getProjectId(),
                entity.getTitle(),
                entity.getMode(),
                entity.getStatus(),
                entity.getModelId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getArchivedAt(),
                entity.getMetadata()
        );
    }
}

