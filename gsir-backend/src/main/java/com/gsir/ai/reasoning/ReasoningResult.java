package com.gsir.ai.reasoning;

import com.gsir.ai.provider.TokenUsage;

public record ReasoningResult(
    String conclusion,
    double confidence,
    java.util.List<String> evidenceCitations,
    java.util.List<String> contradictions,
    java.util.List<String> missingInformation,
    TokenUsage tokenUsage,
    double estimatedCost,
    boolean isCostKnown
) {}
