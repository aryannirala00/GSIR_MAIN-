package com.gsir.tools.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gsir.tools.Tool;
import com.gsir.tools.ToolDefinition;
import com.gsir.tools.provider.ResearchSourceProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.List;

@Component
public class LiteratureSearchTool implements Tool {

    private final ToolDefinition definition;
    private final ResearchSourceProvider provider;

    public LiteratureSearchTool(List<ResearchSourceProvider> providers) {
        this.definition = new ToolDefinition(
                "LITERATURE_SEARCH",
                "1.0",
                "Searches scientific literature.",
                "search",
                "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}},\"required\":[\"query\"]}",
                "{\"type\":\"object\",\"properties\":{\"results\":{\"type\":\"array\"},\"query_executed\":{\"type\":\"string\"}}}",
                true,
                15,
                true
        );
        
        // Find OpenAlex provider (In a real system, you'd pick based on user input or config)
        this.provider = providers.stream()
            .filter(p -> p.getProviderName().equals("OpenAlex"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("OpenAlex provider not found"));
    }

    @Override
    public ToolDefinition getDefinition() {
        return definition;
    }

    @Override
    public JsonNode execute(JsonNode validatedInput, com.gsir.tools.ToolExecutionContext context) throws Exception {
        String query = validatedInput.get("query").asText();
        
        JsonNode providerResults = provider.search(query, 5);
        
        ObjectNode output = JsonNodeFactory.instance.objectNode();
        output.put("query_executed", query);
        output.set("results", providerResults);
        
        return output;
    }
}
