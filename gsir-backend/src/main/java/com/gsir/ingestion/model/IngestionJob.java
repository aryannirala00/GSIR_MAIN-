package com.gsir.ingestion.model;

import java.util.UUID;
import java.time.Instant;
import java.util.List;

public record IngestionJob(
    UUID jobId,
    UUID sourceId,
    String sourceVersion,
    IngestionState state,
    Instant startedAt,
    Instant completedAt,
    String workerId,
    String processingVersion,
    List<String> errors,
    List<String> warnings,
    UUID projectId
) {}
