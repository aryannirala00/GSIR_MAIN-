package com.gsir.experiments.execution;

import com.gsir.experiments.model.ExperimentEntity;
import java.util.UUID;

public interface ExperimentExecutionService {
    String scheduleExecution(ExperimentEntity experiment);
    void pauseExecution(String executionId);
    void cancelExecution(String executionId);
    ExperimentExecutionResult getResult(String executionId);
}
