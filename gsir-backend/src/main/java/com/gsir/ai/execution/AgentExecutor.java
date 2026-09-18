package com.gsir.ai.execution;

public interface AgentExecutor {
    ExecutionState executeTask(String objective, java.util.UUID projectId, java.util.UUID userId, ExecutionBudget budget);
    ExecutionState resumeExecution(ExecutionState state);
}
