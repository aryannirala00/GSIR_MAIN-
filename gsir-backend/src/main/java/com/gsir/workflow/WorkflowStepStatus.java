package com.gsir.workflow;

public enum WorkflowStepStatus {
    PENDING,
    READY,
    RUNNING,
    WAITING_FOR_REVIEW,
    COMPLETED,
    FAILED,
    CANCELLED
}
