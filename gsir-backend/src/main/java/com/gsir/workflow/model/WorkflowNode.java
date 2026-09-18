package com.gsir.workflow.model;

public record WorkflowNode(
    String nodeId,
    String type,
    String name,
    java.util.Map<String, Object> configuration
) {}
