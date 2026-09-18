package com.gsir.provenance.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ProvActivity(
    UUID id,
    UUID projectId,
    String activityType,
    String status,
    Instant startTime,
    Instant endTime,
    Map<String, Object> parameters,
    Map<String, Object> executionEnvironment,
    Instant createdAt
) {}
