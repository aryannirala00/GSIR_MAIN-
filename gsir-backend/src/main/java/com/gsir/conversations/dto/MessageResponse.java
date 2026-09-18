package com.gsir.conversations.dto;

import com.gsir.conversations.MessageEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID conversationId,
        UUID parentMessageId,
        String role,
        String status,
        String modelId,
        Instant createdAt,
        Instant updatedAt,
        String metadata,
        List<MessagePartResponse> parts
) {
    public static MessageResponse fromEntity(MessageEntity entity, List<MessagePartResponse> parts) {
        return new MessageResponse(
                entity.getId(),
                entity.getConversationId(),
                entity.getParentMessageId(),
                entity.getRole(),
                entity.getStatus(),
                entity.getModelId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getMetadata(),
                parts
        );
    }
}

