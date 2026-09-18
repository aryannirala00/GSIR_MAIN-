package com.gsir.workflow.compiler;

import com.gsir.workflow.model.WorkflowPlan;
import com.gsir.workflow.ResearchWorkflowEntity;

public interface WorkflowCompiler {
    WorkflowPlan compile(ResearchWorkflowEntity workflow);
}
