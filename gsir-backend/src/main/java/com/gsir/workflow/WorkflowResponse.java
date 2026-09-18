package com.gsir.workflow;

import java.time.Instant;
import java.util.UUID;

public record WorkflowResponse(
        UUID id,
        UUID projectId,
        String objective,
        WorkflowStatus status,
        Instant createdAt
) {
    public static WorkflowResponse fromEntity(ResearchWorkflowEntity entity) {
        return new WorkflowResponse(
                entity.getId(),
                entity.getProjectId(),
                entity.getObjective(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
