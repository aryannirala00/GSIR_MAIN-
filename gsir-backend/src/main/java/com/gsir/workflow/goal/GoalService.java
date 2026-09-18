package com.gsir.workflow.goal;

import com.gsir.common.error.CommonErrors;
import com.gsir.projects.ProjectAccessService;
import com.gsir.workflow.ResearchWorkflowEntity;
import com.gsir.workflow.ResearchWorkflowRepository;
import com.gsir.workflow.WorkflowStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Part 84 — Goal State Service
 * Maps the AI cognitive Goal state to durable ResearchWorkflows.
 * Ensures goals cannot be created persistently without authorization
 * and policy constraints, preventing runaway AI planning.
 */
@Service
public class GoalService {

    private final ResearchWorkflowRepository workflowRepository;
    private final ProjectAccessService projectAccessService;
    private final com.gsir.projects.ProjectRepository projectRepository;
    private final com.gsir.provenance.repository.ProvenanceRepository provenanceRepository;

    public GoalService(ResearchWorkflowRepository workflowRepository, ProjectAccessService projectAccessService, 
            com.gsir.projects.ProjectRepository projectRepository, com.gsir.provenance.repository.ProvenanceRepository provenanceRepository) {
        this.workflowRepository = workflowRepository;
        this.projectAccessService = projectAccessService;
        this.projectRepository = projectRepository;
        this.provenanceRepository = provenanceRepository;
    }

    /**
     * Converts a WorkflowEntity status to a cognitive GoalStatus.
     */
    private GoalStatus toGoalStatus(WorkflowStatus status) {
        return switch (status) {
            case CREATED, RUNNING -> GoalStatus.ACTIVE;
            case PAUSED -> GoalStatus.BLOCKED;
            case COMPLETED -> GoalStatus.COMPLETED;
            case FAILED -> GoalStatus.FAILED;
            case CANCELLED -> GoalStatus.CANCELLED;
        };
    }

    @Transactional(readOnly = true)
    public Goal getGoal(UUID goalId, UUID projectId, UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        
        ResearchWorkflowEntity workflow = workflowRepository.findById(goalId)
                .orElseThrow(() -> CommonErrors.notFound("Goal (workflow)"));
                
        if (!workflow.getProjectId().equals(projectId)) {
            throw CommonErrors.forbidden();
        }

        // We use an implicit default priority of 1.0 for workflow-backed goals.
        // A complete implementation might serialize this in research_state.
        return new Goal(
                workflow.getId(),
                workflow.getObjective(),
                1.0,
                toGoalStatus(workflow.getStatus())
        );
    }

    /**
     * Policy check and creation of an AI-generated persistent goal.
     */
    @Transactional
    public Goal establishGoal(UUID projectId, UUID userId, String description, double priority) {
        // 1. Authorization Control
        projectAccessService.requireAccess(projectId, userId);
        
        // 2. Concurrency Lock: Lock the project to prevent race conditions during goal limit check
        projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> CommonErrors.notFound("Project not found"));
        
        // 3. Policy Control: Count active goals to prevent unrestricted creation
        // Note: The limit of 5 is an implementation policy defined here to fulfill the requirement:
        // "The system must not allow an AI model to create unrestricted persistent goals."
        long activeCount = workflowRepository.findAll().stream()
                .filter(w -> w.getProjectId().equals(projectId))
                .filter(w -> w.getStatus() == WorkflowStatus.CREATED || w.getStatus() == WorkflowStatus.RUNNING)
                .count();
                
        if (activeCount >= 5) {
            throw CommonErrors.conflict("Policy violation: Project has reached the maximum allowed active goals (5).");
        }
        
        // 4. Durable representation
        UUID goalId = UUID.randomUUID();
        ResearchWorkflowEntity workflow = new ResearchWorkflowEntity(goalId, projectId, userId, description);
        workflowRepository.save(workflow);
        
        // 5. Provenance Generation
        // Goal creation establishes provenance via a PROV Activity that triggered the goal Workflow.
        // We defer full subgraph creation here but document the integration explicitly to satisfy Part 84.
        com.gsir.provenance.model.ProvActivity activity = new com.gsir.provenance.model.ProvActivity(
            UUID.randomUUID(), projectId,
            "GOAL_ESTABLISHMENT",
            "COMPLETED",
            java.time.Instant.now(), java.time.Instant.now(),
            java.util.Map.of("goalId", goalId.toString(), "description", description),
            java.util.Map.of("userId", userId.toString()),
            java.time.Instant.now()
        );
        provenanceRepository.saveActivity(activity);
        
        return new Goal(
                workflow.getId(),
                workflow.getObjective(),
                priority,
                toGoalStatus(workflow.getStatus())
        );
    }
}
