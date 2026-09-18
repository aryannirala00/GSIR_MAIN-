package com.gsir.experiments.execution;

import com.gsir.experiments.model.ExperimentEntity;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.time.Instant;
import java.util.Collections;

@Service
public class DefaultExperimentExecutionService implements ExperimentExecutionService {
    
    @Override
    public String scheduleExecution(ExperimentEntity experiment) {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public void pauseExecution(String executionId) {}
    
    @Override
    public void cancelExecution(String executionId) {}
    
    @Override
    public ExperimentExecutionResult getResult(String executionId) {
        return new ExperimentExecutionResult(
            UUID.fromString(executionId),
            UUID.randomUUID(),
            "COMPLETED",
            Collections.emptyMap(),
            Collections.emptyMap(),
            true,
            Instant.now()
        );
    }
}
