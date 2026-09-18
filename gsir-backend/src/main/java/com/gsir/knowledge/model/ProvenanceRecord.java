package com.gsir.knowledge.model;

import java.util.UUID;
import java.time.Instant;

public record ProvenanceRecord(
    UUID provenanceId,
    UUID sourceId,
    String extractorId, // Tool/AI/Model
    UUID workflowStepId,
    String executionFingerprint,
    Instant extractionTime,
    String confidenceMetric
) {}
