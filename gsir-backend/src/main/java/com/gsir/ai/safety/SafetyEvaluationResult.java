package com.gsir.ai.safety;

public record SafetyEvaluationResult(
    SafetyDecision decision,
    String reason,
    String policyId
) {}
