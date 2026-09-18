package com.gsir.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.jobs.JobEntity;
import com.gsir.jobs.JobExecutionEvent;
import com.gsir.tools.ToolExecutionService;
import com.gsir.execution.service.CostTrackingService;
import com.gsir.execution.service.HumanReviewService;
import com.gsir.execution.model.HumanReviewTaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gsir.provenance.service.ExecutionProvenanceService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class WorkflowStepWorker {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStepWorker.class);

    private final ObjectMapper objectMapper;
    private final WorkflowOrchestrator orchestrator;
    private final ResearchWorkflowStepRepository stepRepository;
    private final ResearchWorkflowRepository workflowRepository;
    private final ToolExecutionService toolExecutionService;
    private final FingerprintService fingerprintService;
    private final CostTrackingService costTrackingService;
    private final HumanReviewService humanReviewService;
    private final ExecutionProvenanceService executionProvenanceService;
    private final com.gsir.projects.ProjectRepository projectRepository;
    private final com.gsir.workspaces.WorkspaceRepository workspaceRepository;

    public WorkflowStepWorker(
            ObjectMapper objectMapper,
            WorkflowOrchestrator orchestrator,
            ResearchWorkflowStepRepository stepRepository,
            ResearchWorkflowRepository workflowRepository,
            ToolExecutionService toolExecutionService,
            FingerprintService fingerprintService,
            CostTrackingService costTrackingService,
            HumanReviewService humanReviewService,
            ExecutionProvenanceService executionProvenanceService,
            com.gsir.projects.ProjectRepository projectRepository,
            com.gsir.workspaces.WorkspaceRepository workspaceRepository) {
        this.objectMapper = objectMapper;
        this.orchestrator = orchestrator;
        this.stepRepository = stepRepository;
        this.workflowRepository = workflowRepository;
        this.toolExecutionService = toolExecutionService;
        this.fingerprintService = fingerprintService;
        this.costTrackingService = costTrackingService;
        this.humanReviewService = humanReviewService;
        this.executionProvenanceService = executionProvenanceService;
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
    }



    @EventListener
    public void onJobExecution(JobExecutionEvent event) {
        JobEntity job = event.getJob();
        if (!"WORKFLOW_STEP".equals(job.getJobType())) {
            return;
        }

        UUID stepId = null;
        ResearchWorkflowStepEntity step = null;
        ResearchWorkflowEntity workflow = null;
        String toolVersion = "1.0";
        try {
            JsonNode payload = objectMapper.readTree(job.getPayload());
            stepId = UUID.fromString(payload.get("stepId").asText());
            
            step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Workflow step not found"));
                
            if (step.getStatus() == WorkflowStepStatus.COMPLETED) {
                log.info("Step {} is already completed. Idempotent skip.", stepId);
                return;
            }
            if (step.getOutputs() != null) {
                log.info("Step {} has outputs but wasn't marked completed. Recovering.", stepId);
                orchestrator.markStepCompleted(stepId, step.getOutputs());
                return;
            }
                
            workflow = workflowRepository.findById(step.getWorkflowId())
                .orElseThrow(() -> new RuntimeException("Workflow not found"));
                
            // Enforce execution budget before execution (financial constraint)
            costTrackingService.enforceBudget(workflow.getProjectId());
            // Note: parallel-job quota is enforced in WorkflowOrchestrator.evaluateAndSchedule
            // before the step transitions to READY. It is not re-checked here to avoid
            // blocking a step that has already been legitimately admitted.
            
            // 1. Generate execution fingerprint for reproducibility and idempotency
            String fingerprint = fingerprintService.generateFingerprint(
                workflow.getProjectId().toString(),
                step.getCapability(),
                toolVersion,
                step.getInputs()
            );
            
            step.setExecutionFingerprint(fingerprint);
            step.setToolVersion(toolVersion);
            
            // 2. Check for identical historical executions (Safe Deduplication)
            List<ResearchWorkflowStepEntity> previousSteps = stepRepository.findByExecutionFingerprintAndStatus(fingerprint, WorkflowStepStatus.COMPLETED);
            if (!previousSteps.isEmpty()) {
                String cachedOutput = previousSteps.get(0).getOutputs();
                log.info("Reusing cached output for step {} with fingerprint {}", stepId, fingerprint);
                orchestrator.markStepCompleted(stepId, cachedOutput);
                return;
            }

            // Determine if step is human approved
            List<HumanReviewTaskEntity> reviews = humanReviewService.getProjectTasks(workflow.getProjectId());
            boolean approved = false;
            boolean rejected = false;
            for (HumanReviewTaskEntity review : reviews) {
                if (review.getStepId().equals(step.getId()) && "RESOLVED".equals(review.getStatus())) {
                    if (review.getDecision() != null && Boolean.TRUE.equals(review.getDecision().get("approved"))) {
                        approved = true;
                    } else {
                        rejected = true;
                    }
                }
            }

            // Human-in-the-loop Approval Gate
            if ("HUMAN_APPROVAL".equals(step.getCapability())) {
                if (step.getStatus() != WorkflowStepStatus.WAITING_FOR_REVIEW) {
                    step.setStatus(WorkflowStepStatus.WAITING_FOR_REVIEW);
                    stepRepository.save(step);
                    
                    humanReviewService.createReviewTask(workflow.getProjectId(), workflow.getId(), step.getId(), 
                        Map.of("message", "Approval required for workflow " + workflow.getId()));
                }
                
                if (approved) {
                    orchestrator.markStepCompleted(stepId, "{\"result\": \"approved\"}");
                    return;
                } else if (rejected) {
                    throw new RuntimeException("Workflow step rejected by human review");
                } else {
                    throw new RuntimeException("WAITING_FOR_HUMAN_REVIEW");
                }
            }
            
            if (rejected) {
                throw new RuntimeException("Workflow step rejected by human review");
            }
            if (step.getStatus() == WorkflowStepStatus.WAITING_FOR_REVIEW && !approved) {
                throw new RuntimeException("WAITING_FOR_HUMAN_REVIEW");
            }

            // Ensure status update
            step.setStatus(WorkflowStepStatus.RUNNING);
            stepRepository.save(step);

            // Execute Capability through ToolExecutionService
            long startNanos = System.nanoTime();
            
            com.gsir.projects.ProjectEntity project = projectRepository.findById(workflow.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
            com.gsir.workspaces.WorkspaceEntity workspace = workspaceRepository.findById(project.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
            
            // Re-use Job ID as Request ID to trace async execution appropriately
            com.gsir.tools.ToolExecutionContext context = new com.gsir.tools.ToolExecutionContext(
                workflow.getUserId(),
                workspace.getOrganizationId(),
                workspace.getId(),
                workflow.getProjectId(),
                job.getId(), // Use Job ID as requestId
                workflow.getId(), // Use Workflow ID as aiRunId
                approved, // humanApproved
                step.getId() // stepId
            );

            try {
                String output = toolExecutionService.executeTool(
                    step.getCapability(), toolVersion, 
                    step.getInputs(), 
                    context
                );
                
                // Record Cost
                long durationMs = (System.nanoTime() - startNanos) / 1000000;
                double computeCost = durationMs * 0.0001; 
                double modelCost = output.length() * 0.00005; // 0.00005 per output char
                costTrackingService.recordCost(workflow.getProjectId(), workflow.getId(), stepId, computeCost, modelCost, 0.0, "USD");

                // Part 80.17/80.19/80.91 — Record execution provenance
                executionProvenanceService.recordStepExecution(
                    workflow.getId(), stepId, step.getCapability(), toolVersion,
                    step.getInputs(), output, workflow.getProjectId(), null
                );
                
                orchestrator.markStepCompleted(stepId, output);
            } catch (com.gsir.ai.safety.RequireHumanReviewException hre) {
                step.setStatus(WorkflowStepStatus.WAITING_FOR_REVIEW);
                stepRepository.save(step);
                humanReviewService.createReviewTask(workflow.getProjectId(), workflow.getId(), step.getId(), 
                        Map.of("message", hre.getMessage(), "fingerprint", fingerprint));
                throw new RuntimeException("WAITING_FOR_HUMAN_REVIEW");
            }
            
        } catch (Exception e) {
            log.error("Workflow step execution failed for job {}", job.getId(), e);
            
            // Record failure provenance
            try {
                if (workflow != null && step != null) {
                    executionProvenanceService.recordStepExecution(
                        workflow.getId(), stepId, step.getCapability(), toolVersion,
                        step.getInputs(), "ERROR: " + e.getMessage(), workflow.getProjectId(), null
                    );
                }
            } catch (Exception ex) {
                log.warn("Failed to record failure provenance", ex);
            }
            
            throw new RuntimeException("Workflow step failed: " + e.getMessage(), e);
        }
    }
}


