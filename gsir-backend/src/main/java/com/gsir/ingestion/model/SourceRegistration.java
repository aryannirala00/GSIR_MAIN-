package com.gsir.ingestion.model;

import java.time.Instant;
import java.util.UUID;

public record SourceRegistration(
    UUID sourceId,
    SourceType sourceType,
    String provider,
    String locationUrl,
    String accessMethod,
    String license,
    String status,
    UUID projectId,
    Instant registeredAt
) {}
