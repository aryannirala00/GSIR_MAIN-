package com.gsir.workflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.error.CommonErrors;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.jobs.JobEntity;
import com.gsir.jobs.JobService;
import com.gsir.projects.ProjectAccessService;
import com.gsir.resources.ResourceQuotaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.gsir.workflow.compiler.WorkflowValidator;
import com.gsir.workflow.compiler.WorkflowCompiler;
import com.gsir.workflow.model.WorkflowPlan;

@Service
public class WorkflowOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(WorkflowOrchestrator.class);

    private final ResearchWorkflowRepository workflowRepository;
    private final ResearchWorkflowStepRepository stepRepository;
    private final JobService jobService;
    private final ProjectAccessService projectAccessService;
    private final UuidV7Generator idGenerator;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private final WorkflowValidator workflowValidator;
    private final WorkflowCompiler workflowCompiler;
    private final ResourceQuotaService resourceQuotaService;

        @org.springframework.context.event.EventListener
    public void onJobFailed(com.gsir.jobs.JobFailedEvent event) {
        if (!"WORKFLOW_STEP".equals(event.getJob().getJobType())) return;
        try {
            com.fasterxml.jackson.databind.JsonNode payload = objectMapper.readTree(event.getJob().getPayload());
            java.util.UUID stepId = java.util.UUID.fromString(payload.get("stepId").asText());
            markStepFailed(stepId, "Max attempts reached for job " + event.getJob().getId());
        } catch (Exception e) {
            log.error("Failed to parse failed workflow step job", e);
        }
    }

    public WorkflowOrchestrator(
            ResearchWorkflowRepository workflowRepository,
            ResearchWorkflowStepRepository stepRepository,
            JobService jobService,
            ProjectAccessService projectAccessService,
            UuidV7Generator idGenerator,
            TransactionTemplate transactionTemplate,
            ObjectMapper objectMapper,
            WorkflowValidator workflowValidator,
            WorkflowCompiler workflowCompiler,
            ResourceQuotaService resourceQuotaService) {
        this.workflowRepository = workflowRepository;
        this.stepRepository = stepRepository;
        this.jobService = jobService;
        this.projectAccessService = projectAccessService;
        this.idGenerator = idGenerator;
        this.transactionTemplate = transactionTemplate;
        this.objectMapper = objectMapper;
        this.workflowValidator = workflowValidator;
        this.workflowCompiler = workflowCompiler;
        this.resourceQuotaService = resourceQuotaService;
    }


    public ResearchWorkflowEntity createWorkflow(UUID projectId, UUID userId, String idempotencyKey, CreateWorkflowRequest request) {
        projectAccessService.requireAccess(projectId, userId);

        if (request.objective() == null || request.objective().isBlank()) {
            throw CommonErrors.invalid("Workflow objective cannot be empty");
        }

        return transactionTemplate.execute(status -> {
            UUID workflowId = idGenerator.generate();
            ResearchWorkflowEntity workflow = new ResearchWorkflowEntity(workflowId, projectId, userId, request.objective());
            workflowRepository.save(workflow);
            
            Map<String, UUID> stepNameToId = new HashMap<>();
            List<ResearchWorkflowStepEntity> entities = new ArrayList<>();
            
            for (CreateWorkflowRequest.StepRequest stepReq : request.steps()) {
                UUID stepId = idGenerator.generate();
                stepNameToId.put(stepReq.stepName(), stepId);
                
                String depsJson = "[]";
                try {
                    depsJson = objectMapper.writeValueAsString(stepReq.dependencies());
                } catch (Exception e) {}
                
                ResearchWorkflowStepEntity stepEntity = new ResearchWorkflowStepEntity(
                        stepId, workflowId, stepReq.stepName(), stepReq.capability(), stepReq.inputs(), depsJson
                );
                entities.add(stepEntity);
            }
            
            for (ResearchWorkflowStepEntity step : entities) {
                try {
                    List<String> depNames = objectMapper.readValue(step.getDependencies(), new TypeReference<List<String>>() {});
                    List<String> depIds = new ArrayList<>();
                    for (String name : depNames) {
                        UUID depId = stepNameToId.get(name);
                        if (depId != null) {
                            depIds.add(depId.toString());
                        } else {
                            throw CommonErrors.invalid("Unknown dependency: " + name);
                        }
                    }
                    step.setDependencies(objectMapper.writeValueAsString(depIds));
                } catch (Exception e) {
                    throw CommonErrors.invalid("Failed to parse dependencies for step " + step.getStepName());
                }
                stepRepository.save(step);
            }
            
            return workflow;
        });
    }

    @Transactional
    public void startWorkflow(UUID workflowId, UUID userId) {
        ResearchWorkflowEntity workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> CommonErrors.notFound("Workflow not found"));
                
        projectAccessService.requireAccess(workflow.getProjectId(), userId);

        workflowValidator.validate(workflow);
        
        workflow.setStatus(WorkflowStatus.RUNNING);
        workflowRepository.save(workflow);
        
        WorkflowPlan plan = workflowCompiler.compile(workflow);
        log.info("Compiled WorkflowPlan execution order: {}", plan.executionOrder());
        
        evaluateAndSchedule(workflowId);
    }

    @Transactional
    public void pauseWorkflow(UUID workflowId, UUID userId) {
        ResearchWorkflowEntity workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> CommonErrors.notFound("Workflow not found"));
        projectAccessService.requireAccess(workflow.getProjectId(), userId);
        
        if (workflow.getStatus() == WorkflowStatus.RUNNING) {
            workflow.setStatus(WorkflowStatus.PAUSED);
            workflowRepository.save(workflow);
        }
    }

    @Transactional
    public void resumeWorkflow(UUID workflowId, UUID userId) {
        ResearchWorkflowEntity workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> CommonErrors.notFound("Workflow not found"));
        projectAccessService.requireAccess(workflow.getProjectId(), userId);
        
        if (workflow.getStatus() == WorkflowStatus.PAUSED) {
            workflow.setStatus(WorkflowStatus.RUNNING);
            workflowRepository.save(workflow);
            evaluateAndSchedule(workflowId);
        }
    }

    @Transactional
    public void cancelWorkflow(UUID workflowId, UUID userId) {
        ResearchWorkflowEntity workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> CommonErrors.notFound("Workflow not found"));
        projectAccessService.requireAccess(workflow.getProjectId(), userId);
        
        if (workflow.getStatus() == WorkflowStatus.RUNNING || workflow.getStatus() == WorkflowStatus.PAUSED) {
            workflow.setStatus(WorkflowStatus.CANCELLED);
            workflowRepository.save(workflow);
            
            List<ResearchWorkflowStepEntity> steps = stepRepository.findByWorkflowId(workflowId);
            for (ResearchWorkflowStepEntity step : steps) {
                if (step.getStatus() == WorkflowStepStatus.PENDING || step.getStatus() == WorkflowStepStatus.READY || step.getStatus() == WorkflowStepStatus.RUNNING) {
                    step.setStatus(WorkflowStepStatus.CANCELLED);
                    stepRepository.save(step);
                    // In a full implementation, we'd also cancel the underlying Jobs in JobService here.
                }
            }
        }
    }
    
    @Transactional
    public void evaluateAndSchedule(UUID workflowId) {
        ResearchWorkflowEntity workflow = workflowRepository.findByIdForUpdate(workflowId)
                .orElseThrow(() -> CommonErrors.notFound("Workflow not found"));

        if (workflow.getStatus() == WorkflowStatus.PAUSED || workflow.getStatus() == WorkflowStatus.CANCELLED) {
            return;
        }
                
        List<ResearchWorkflowStepEntity> allSteps = stepRepository.findByWorkflowId(workflowId);
        
        boolean allCompleted = true;
        boolean anyFailed = false;

        for (ResearchWorkflowStepEntity step : allSteps) {
            if (step.getStatus() == WorkflowStepStatus.FAILED) {
                anyFailed = true;
                break;
            }
            if (step.getStatus() != WorkflowStepStatus.COMPLETED) {
                allCompleted = false;
            }
        }
        
        if (anyFailed) {
            workflow.setStatus(WorkflowStatus.FAILED);
            workflowRepository.save(workflow);
            return;
        }
        if (allCompleted) {
            workflow.setStatus(WorkflowStatus.COMPLETED);
            workflowRepository.save(workflow);
            return;
        }

        for (ResearchWorkflowStepEntity step : allSteps) {
            if (step.getStatus() == WorkflowStepStatus.PENDING) {
                if (areDependenciesMet(step, allSteps)) {
                    // Enforce parallel-job quota before admitting the step.
                    // If quota is exceeded, leave step PENDING — it will be retried
                    // when a running step completes and calls evaluateAndSchedule again.
                    try {
                        resourceQuotaService.enforceParallelJobQuota(workflow.getProjectId());
                    } catch (com.gsir.resources.QuotaExceededException e) {
                        log.info("Parallel job quota exceeded for project {}; step {} remains PENDING: {}",
                            workflow.getProjectId(), step.getId(), e.getMessage());
                        continue; // leave this step PENDING; try other steps
                    }
                    step.setStatus(WorkflowStepStatus.READY);
                    stepRepository.save(step);
                    scheduleStep(workflow, step);
                }
            }
        }
    }
    
    private boolean areDependenciesMet(ResearchWorkflowStepEntity step, List<ResearchWorkflowStepEntity> allSteps) {
        try {
            List<String> depIds = objectMapper.readValue(step.getDependencies(), new TypeReference<List<String>>() {});
            if (depIds.isEmpty()) return true;
            
            for (String depId : depIds) {
                boolean met = allSteps.stream()
                        .anyMatch(s -> s.getId().toString().equals(depId) && s.getStatus() == WorkflowStepStatus.COMPLETED);
                if (!met) return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to parse dependencies for step {}", step.getId(), e);
            return false;
        }
    }
    
    private void scheduleStep(ResearchWorkflowEntity workflow, ResearchWorkflowStepEntity step) {
        String payload = String.format("{\"workflowId\":\"%s\",\"stepId\":\"%s\",\"userId\":\"%s\"}", 
            workflow.getId(), step.getId(), workflow.getUserId());
            
        String idempotencyKey = "wfstep-" + step.getId();
        JobEntity job = jobService.submitJob(workflow.getUserId(), idempotencyKey, "WORKFLOW_STEP", payload);
        
        step.setJobId(job.getId());
        stepRepository.save(step);
    }
    
    @Transactional
    public void markStepCompleted(UUID stepId, String outputs) {
        ResearchWorkflowStepEntity step = stepRepository.findById(stepId).orElseThrow();
        step.setStatus(WorkflowStepStatus.COMPLETED);
        step.setOutputs(outputs);
        stepRepository.save(step);
        
        evaluateAndSchedule(step.getWorkflowId());
    }

    @Transactional
    public void markStepFailed(UUID stepId, String error) {
        ResearchWorkflowStepEntity step = stepRepository.findById(stepId).orElseThrow();
        step.setStatus(WorkflowStepStatus.FAILED);
        step.setOutputs(error);
        stepRepository.save(step);
        
        evaluateAndSchedule(step.getWorkflowId());
    }
}



