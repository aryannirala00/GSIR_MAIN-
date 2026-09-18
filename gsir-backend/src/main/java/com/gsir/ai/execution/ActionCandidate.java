package com.gsir.ai.execution;

import java.util.Map;

public record ActionCandidate(
        String tool,
        Map<String, Object> arguments,
        double expectedUtility,
        double risk,
        double cost) {
    
    public ActionCandidate {
        if (tool == null || tool.isBlank()) throw new IllegalArgumentException("tool cannot be empty");
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
}

