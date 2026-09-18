package com.gsir.tools;

import com.gsir.common.error.CommonErrors;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ToolRegistry {

    // Key: toolId@version
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    public ToolRegistry(List<Tool> availableTools) {
        for (Tool tool : availableTools) {
            ToolDefinition def = tool.getDefinition();
            String key = def.toolId() + "@" + def.version();
            if (tools.containsKey(key)) {
                throw new IllegalStateException("Duplicate tool registered: " + key);
            }
            tools.put(key, tool);
        }
    }

    public Tool getTool(String toolId, String version) {
        String key = toolId + "@" + version;
        Tool tool = tools.get(key);
        if (tool == null) {
            throw CommonErrors.invalid("Tool not found or unsupported version: " + key);
        }
        if (!tool.getDefinition().enabled()) {
            throw CommonErrors.invalid("Tool is currently disabled: " + key);
        }
        return tool;
    }

    public List<ToolDefinition> getAllDefinitions() {
        return tools.values().stream().map(Tool::getDefinition).toList();
    }
}
