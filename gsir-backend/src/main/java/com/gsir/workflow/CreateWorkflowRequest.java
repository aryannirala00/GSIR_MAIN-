package com.gsir.workflow;

import java.util.List;

public record CreateWorkflowRequest(
        String objective,
        List<StepRequest> steps
) {
    public record StepRequest(
            String stepName,
            String capability,
            String inputs,
            List<String> dependencies
    ) {}
}
