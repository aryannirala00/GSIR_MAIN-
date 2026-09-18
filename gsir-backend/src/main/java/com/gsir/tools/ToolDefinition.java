package com.gsir.tools;

public record ToolDefinition(
        String toolId,
        String version,
        String description,
        String capabilityCategory,
        String inputSchema,
        String outputSchema,
        boolean requiresNetwork,
        int timeoutSeconds,
        boolean enabled
) {}
