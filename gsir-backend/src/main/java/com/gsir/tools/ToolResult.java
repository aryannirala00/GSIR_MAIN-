package com.gsir.tools;

import com.fasterxml.jackson.databind.JsonNode;

public record ToolResult(
    JsonNode data,
    ToolExecutionContext context,
    String executionProvenance
) {
}

