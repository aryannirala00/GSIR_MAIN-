package com.gsir.ai.metacognition;

public record MetaAssessment(
        double confidence,
        double uncertainty,
        boolean requiresMoreEvidence,
        boolean requiresToolUse,
        boolean requiresHumanReview) {
}

