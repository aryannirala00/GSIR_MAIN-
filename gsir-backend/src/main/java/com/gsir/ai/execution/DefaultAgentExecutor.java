package com.gsir.ai.execution;

import com.gsir.ai.planning.Plan;
import com.gsir.ai.planning.Planner;
import com.gsir.ai.context.ContextBuilder;
import com.gsir.ai.reasoning.ReasoningEngine;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import com.gsir.ai.metacognition.MetacognitionService;
import com.gsir.ai.metacognition.MetaAssessment;
import com.gsir.ai.reasoning.ReasoningResult;
import java.util.List;

@Service
public class DefaultAgentExecutor implements AgentExecutor {

    private final Planner planner;
    private final ContextBuilder contextBuilder;
    private final ReasoningEngine reasoningEngine;
    private final MetacognitionService metacognitionService;
    private final ExecutionBudgetService budgetService;

    public DefaultAgentExecutor(Planner planner, ContextBuilder contextBuilder, ReasoningEngine reasoningEngine, MetacognitionService metacognitionService, ExecutionBudgetService budgetService) {
        this.planner = planner;
        this.contextBuilder = contextBuilder;
        this.reasoningEngine = reasoningEngine;
        this.metacognitionService = metacognitionService;
        this.budgetService = budgetService;
    }

    @Override
    public ExecutionState executeTask(String objective, UUID projectId, UUID userId, ExecutionBudget budget) {
        // 63.86 Research Agent Loop (Mock initialization)
        Plan plan = planner.createPlan(objective, projectId);
        UUID executionId = UUID.randomUUID();
        
        budgetService.createBudget(executionId, projectId, budget);
        
        return new ExecutionState(
                executionId,
                objective,
                plan,
                plan.steps().isEmpty() ? null : plan.steps().get(0).id(),
                new HashMap<>(),
                new HashMap<>(),
                budget.maxSteps(),
                "INITIALIZED",
                projectId,
                userId
        );
    }

    @Override
    public ExecutionState resumeExecution(ExecutionState state) {
        try {
            budgetService.validateGuard(state.executionId());
            budgetService.consumeStep(state.executionId());
        } catch (IllegalStateException e) {
            return new ExecutionState(
                state.executionId(), state.objective(), state.plan(), state.currentStepId(),
                state.evidence(), state.toolResults(), state.remainingBudget(), "BUDGET_EXHAUSTED",
                state.projectId(), state.userId()
            );
        }

        // 1. Build Context
        String context = contextBuilder.assembleContext("systemPolicy", state.objective(), List.of(), List.copyOf(state.evidence().values()), List.copyOf(state.toolResults().values()), state.plan());

        // 2. Reason
        ReasoningResult result = reasoningEngine.evaluate(context, state.objective());
        
        // 2.5 Consume Tokens and Cost
        if (result.tokenUsage() != null && result.tokenUsage().isAvailable()) {
            budgetService.consumeTokens(state.executionId(), result.tokenUsage().totalTokens());
        }
        if (result.isCostKnown()) {
            budgetService.consumeCost(state.executionId(), result.estimatedCost());
        }

        // 3. Metacognitive Evaluation
        MetaAssessment assessment = metacognitionService.evaluate(state, result, state.userId(), state.projectId());

        // 4. Act upon Metacognitive Assessment
        String nextStatus = "CONTINUE";
        if (assessment.requiresHumanReview()) {
            nextStatus = "AWAITING_HUMAN_REVIEW";
        } else if (assessment.requiresMoreEvidence()) {
            nextStatus = "NEEDS_EVIDENCE";
        } else if (assessment.requiresToolUse()) {
            nextStatus = "NEEDS_TOOL_USE";
        }

        return new ExecutionState(
                state.executionId(), state.objective(), state.plan(), state.currentStepId(),
                state.evidence(), state.toolResults(), state.remainingBudget() - 1, nextStatus,
                state.projectId(), state.userId()
        );
    }
}
