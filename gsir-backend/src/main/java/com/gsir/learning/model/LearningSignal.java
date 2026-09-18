package com.gsir.learning.model;

import java.util.List;
import java.util.UUID;

public record LearningSignal(
    UUID signalId,
    String observation,
    String prediction,
    String action,
    String result,
    String evaluation,
    String predictionError,
    List<KnowledgeUpdate> knowledgeUpdates,
    List<String> newHypotheses,
    UUID aiRunId,
    UUID projectId,
    UUID userId
) {}

