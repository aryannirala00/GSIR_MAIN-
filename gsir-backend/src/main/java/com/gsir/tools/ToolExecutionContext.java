package com.gsir.tools;

import java.util.UUID;

/**
 * Provides the security and execution context for every tool invocation.
 * (GSIR Master Coding Document Part 92)
 */
public record ToolExecutionContext(
        UUID userId,
        UUID organizationId,
        UUID workspaceId,
        UUID projectId,
        UUID requestId,
        UUID aiRunId,
        boolean humanApproved,
        UUID stepId) {
    
    public ToolExecutionContext(UUID userId, UUID organizationId, UUID workspaceId, UUID projectId, UUID requestId, UUID aiRunId) {
        this(userId, organizationId, workspaceId, projectId, requestId, aiRunId, false, null);
    }
    
    public ToolExecutionContext {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        // projectId might be required depending on context, but at least userId is strictly required for auth
    }
}

