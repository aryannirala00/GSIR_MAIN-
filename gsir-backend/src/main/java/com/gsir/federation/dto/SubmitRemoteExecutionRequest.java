package com.gsir.federation.dto;

import java.util.UUID;

public class SubmitRemoteExecutionRequest {
    private UUID workflowId;
    private String payload;

    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}

