package com.gsir.ingestion.model;

import java.util.UUID;
import java.time.Instant;

public record DataLineage(
    UUID lineageId,
    UUID sourceObjectId,
    UUID derivedObjectId,
    String transformationType,
    String processingVersion,
    Instant timestamp,
    UUID projectId
) {}
