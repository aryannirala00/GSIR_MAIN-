package com.gsir.tools.api;

import com.gsir.common.api.ApiResponse;
import com.gsir.tools.ToolDefinition;
import com.gsir.tools.ToolRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tools")
public class ToolController {

    private final ToolRegistry toolRegistry;

    public ToolController(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ToolDefinition>>> getAvailableTools() {
        return ResponseEntity.ok(ApiResponse.ok(toolRegistry.getAllDefinitions()));
    }
}
