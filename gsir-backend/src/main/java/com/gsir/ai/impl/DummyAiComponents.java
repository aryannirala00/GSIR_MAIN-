package com.gsir.ai.impl;

import com.gsir.ai.routing.ModelRouter;
import com.gsir.ai.routing.ModelTier;
import com.gsir.ai.planning.Planner;
import com.gsir.ai.planning.Plan;
import com.gsir.ai.context.ContextBuilder;
import com.gsir.ai.reasoning.ReasoningEngine;
import com.gsir.ai.reasoning.ReasoningResult;
import com.gsir.ai.reasoning.Claim;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.UUID;

@Component
public class DummyAiComponents implements ModelRouter, Planner, ContextBuilder, ReasoningEngine {

    @Override
    public String selectModel(ModelTier minimumTier, Map<String, Object> taskConstraints) {
        return "provider-default-model";
    }

    @Override
    public Plan createPlan(String objective, UUID projectId) {
        // Mock execution constraints for Part 89
        com.gsir.ai.planning.ExecutionConstraints constraints = new com.gsir.ai.planning.ExecutionConstraints(10, 60000L, 5.0, 20, 5, 1024L, 10);
        return new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(), constraints);
    }

    @Override
    public Plan adaptPlan(Plan currentPlan, String newInformation) {
        return currentPlan;
    }

    @Override
    public String assembleContext(String policy, String task, List<Object> mem, List<Object> ev, List<Object> tool, Plan plan) {
        return "Assembled Context";
    }

    @Override
    public ReasoningResult evaluate(String context, String objective) {
        return new ReasoningResult("Conclusion", 0.9, List.of(), List.of(), List.of(),
                new com.gsir.ai.provider.TokenUsage(100, 50, 150, true), 0.05, true);
    }

    @Override
    public Claim verifyClaim(Claim claim, List<Object> independentEvidence) {
        return new Claim(claim.statement(), claim.confidence(), claim.supportingSources(), claim.contradictingSources(), "VERIFIED");
    }
}
