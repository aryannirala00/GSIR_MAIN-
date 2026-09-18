package com.gsir.ai.orchestration;

import com.gsir.ai.execution.ExecutionState;
import java.util.UUID;
import java.util.Map;

public interface AiOrchestrator {
    ExecutionState dispatch(String userRequest, UUID projectId, UUID userId, Map<String, Object> constraints);
}
