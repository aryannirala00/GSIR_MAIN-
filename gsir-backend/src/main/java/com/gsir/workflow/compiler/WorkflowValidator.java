package com.gsir.workflow.compiler;

import com.gsir.workflow.ResearchWorkflowEntity;

public interface WorkflowValidator {
    boolean validate(ResearchWorkflowEntity workflow);
}
