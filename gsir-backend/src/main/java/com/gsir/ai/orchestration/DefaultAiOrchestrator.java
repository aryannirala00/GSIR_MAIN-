package com.gsir.ai.orchestration;

import com.gsir.ai.execution.AgentExecutor;
import com.gsir.ai.execution.ExecutionBudget;
import com.gsir.ai.execution.ExecutionState;
import com.gsir.ai.routing.ModelRouter;
import com.gsir.ai.routing.ModelTier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class DefaultAiOrchestrator implements AiOrchestrator {

    private final AgentExecutor agentExecutor;
    private final ModelRouter modelRouter;

    public DefaultAiOrchestrator(AgentExecutor agentExecutor, ModelRouter modelRouter) {
        this.agentExecutor = agentExecutor;
        this.modelRouter = modelRouter;
    }

    @Override
    public ExecutionState dispatch(String userRequest, UUID projectId, UUID userId, Map<String, Object> constraints) {
        // 63.8 Task Classification & Complexity Estimation
        boolean isComplex = constraints.containsKey("complex") && (boolean) constraints.get("complex");
        int maxSteps = isComplex ? 50 : 5; // 63.50 Adaptive Budgeting
        
        ExecutionBudget budget = new ExecutionBudget(
                maxSteps, 100, 10, 3600000L, 1000000L, 10.0
        );

        // 63.6 Model Routing
        ModelTier tier = isComplex ? ModelTier.TIER_3_REASONING : ModelTier.TIER_1_FAST;
        String modelId = modelRouter.selectModel(tier, constraints);

        // 63.11 Complex Execution Path
        return agentExecutor.executeTask(userRequest, projectId, userId, budget);
    }
}
