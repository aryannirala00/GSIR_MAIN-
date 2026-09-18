package com.gsir.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gsir.tools.Tool;
import com.gsir.tools.ToolDefinition;
import com.gsir.tools.ToolExecutionContext;
import org.springframework.stereotype.Component;

@Component
@Deprecated // Non-production mock tool
public class SummarizeTool implements Tool {

    private final ToolDefinition definition;

    public SummarizeTool() {
        this.definition = new ToolDefinition(
                "SUMMARIZE",
                "1.0",
                "Summarizes research context.",
                "nlp",
                "{\"type\":\"object\",\"properties\":{\"text\":{\"type\":\"string\"}}}",
                "{\"type\":\"object\",\"properties\":{\"summary\":{\"type\":\"string\"}}}",
                false,
                10,
                true
        );
    }

    @Override
    public ToolDefinition getDefinition() {
        return definition;
    }

    @Override
    public JsonNode execute(JsonNode validatedInput, ToolExecutionContext context) throws Exception {
        ObjectNode output = JsonNodeFactory.instance.objectNode();
        output.put("summary", "Simulated summary of research");
        output.put("length", 150);
        return output;
    }
}
