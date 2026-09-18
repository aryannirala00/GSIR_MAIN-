package com.gsir.ai.improvement;

import java.util.UUID;

public record SelfImprovementProposal(
    UUID id,
    UUID projectId,
    SelfImprovementCategory category,
    String description,
    String baselineId, // ID of the baseline experiment/metric
    String candidatePayload, // The proposed change (e.g. new prompt, new strategy)
    String expectedBenefit,
    String testPlan,
    String rollbackPlan
) {}
