package com.gsir.tools;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public interface Tool {
    ToolDefinition getDefinition();
    
    // Execute tool, throws Exception on failure or validation error
    JsonNode execute(JsonNode validatedInput, ToolExecutionContext context) throws Exception;
}
