package com.gsir.workflow.model;

import java.util.List;
import java.util.Map;

public record WorkflowPlan(
    List<String> executionOrder,
    List<WorkflowNode> runnableNodes,
    Map<String, List<String>> dependencies,
    Map<String, Object> resolvedParameters,
    Map<String, String> resourceRequirements,
    Map<String, String> policies
) {}
