package com.gsir.files.dto;

import com.gsir.files.FileEntity;

import java.time.Instant;
import java.util.UUID;

public record FileResponse(
        UUID id,
        UUID projectId,
        UUID userId,
        String name,
        String mimeType,
        Long sizeBytes,
        String status,
        Instant createdAt
) {
    public static FileResponse from(FileEntity entity) {
        return new FileResponse(
                entity.getId(),
                entity.getProjectId(),
                entity.getUserId(),
                entity.getName(),
                entity.getMimeType(),
                entity.getSizeBytes(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}

