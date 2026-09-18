package com.gsir.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResearchWorkflowStepRepository extends JpaRepository<ResearchWorkflowStepEntity, UUID> {

    List<ResearchWorkflowStepEntity> findByWorkflowId(UUID workflowId);

    List<ResearchWorkflowStepEntity> findByWorkflowIdAndStatus(UUID workflowId, WorkflowStepStatus status);

    List<ResearchWorkflowStepEntity> findByExecutionFingerprintAndStatus(String fingerprint, WorkflowStepStatus status);

    /**
     * Count workflow steps for a specific project that are in an admitted (active) state.
     * <p>
     * "Admitted" means READY or RUNNING — both represent a reserved execution slot.
     * READY: the step has been scheduled (job submitted) but may not yet have a worker.
     * RUNNING: the step is actively executing.
     * <p>
     * This query is the authoritative project-scoped parallel-job count used by
     * ResourceQuotaService. It traverses: research_workflow_steps → research_workflows (project_id).
     * It does NOT use the global jobs table.
     */
    @Query(
        "SELECT COUNT(s) FROM ResearchWorkflowStepEntity s " +
        "WHERE s.status IN (:statuses) " +
        "AND s.workflowId IN (" +
        "   SELECT w.id FROM ResearchWorkflowEntity w WHERE w.projectId = :projectId" +
        ")"
    )
    long countAdmittedStepsByProjectId(
        @Param("projectId") UUID projectId,
        @Param("statuses") List<WorkflowStepStatus> statuses
    );
}
