package com.gsir.experiments.execution;

import java.util.UUID;
import java.time.Instant;
import java.util.Map;

public record ExperimentExecutionResult(
    UUID executionId,
    UUID experimentId,
    String status,
    Map<String, String> measurements,
    Map<String, String> analysisOutputs,
    boolean isValidated,
    Instant completedAt
) {}
