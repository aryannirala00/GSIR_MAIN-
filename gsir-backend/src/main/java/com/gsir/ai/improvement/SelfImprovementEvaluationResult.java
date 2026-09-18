package com.gsir.ai.improvement;

public record SelfImprovementEvaluationResult(
    boolean approved,
    boolean requiresHumanReview,
    boolean regressionDetected,
    String reason,
    String evaluationMetrics
) {}
