package com.gsir.files.dto;

import com.gsir.files.FileVersionEntity;

import java.time.Instant;
import java.util.UUID;

public record FileVersionResponse(
        UUID id,
        UUID fileId,
        Integer versionNumber,
        String checksumSha256,
        Long sizeBytes,
        Instant createdAt
) {
    public static FileVersionResponse from(FileVersionEntity entity) {
        return new FileVersionResponse(
                entity.getId(),
                entity.getFileId(),
                entity.getVersionNumber(),
                entity.getChecksumSha256(),
                entity.getSizeBytes(),
                entity.getCreatedAt()
        );
    }
}

