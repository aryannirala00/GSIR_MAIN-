package com.gsir.conversations.dto;

import com.gsir.conversations.MessagePartEntity;
import java.util.UUID;

public record MessagePartResponse(
        UUID id,
        UUID messageId,
        String partType,
        String content,
        String metadata,
        Integer sequenceNumber
) {
    public static MessagePartResponse fromEntity(MessagePartEntity entity) {
        return new MessagePartResponse(
                entity.getId(),
                entity.getMessageId(),
                entity.getPartType(),
                entity.getContent(),
                entity.getMetadata(),
                entity.getSequenceNumber()
        );
    }
}

