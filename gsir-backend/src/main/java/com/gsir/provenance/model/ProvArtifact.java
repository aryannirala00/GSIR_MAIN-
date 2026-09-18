package com.gsir.provenance.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ProvArtifact(
    UUID id,
    UUID projectId,
    String artifactType,
    String contentHash,
    Integer versionNumber,
    String locationUri,
    Map<String, Object> metadata,
    Instant createdAt
) {}
