package com.gsir.resources;

import com.gsir.workflow.ResearchWorkflowStepRepository;
import com.gsir.workflow.WorkflowStepStatus;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Part 79 — GSIR Resource Quota Service.
 *
 * Admission flow (per requirement):
 *   1. Lock the quota row with PESSIMISTIC_WRITE (serialises concurrent workers for the same project).
 *   2. Count authoritative project-scoped admitted steps (READY + RUNNING) via ResearchWorkflowStepRepository.
 *   3. Reject if count >= maxParallelJobs by throwing QuotaExceededException (GsirException, HTTP 429).
 *
 * Project isolation:
 *   The active count is strictly scoped to the project — it traverses research_workflow_steps
 *   → research_workflows → project_id. Project B is never affected by Project A's quota.
 *
 * H2 vs PostgreSQL:
 *   Functional correctness (admission threshold, cross-project isolation, null=unlimited) is
 *   verified by H2 integration tests. The pessimistic lock provides PostgreSQL-level serialisation
 *   under production load; H2 does not enforce SELECT FOR UPDATE in the same way.
 *
 * Quota validation:
 *   Negative values are rejected at write time by setQuota().
 *   Null means unlimited (no constraint applied).
 */
@Service
public class ResourceQuotaService {

    private static final Logger log = LoggerFactory.getLogger(ResourceQuotaService.class);

    /** Statuses that represent an actively admitted execution slot. */
    private static final List<WorkflowStepStatus> ADMITTED_STATUSES =
        List.of(WorkflowStepStatus.READY, WorkflowStepStatus.RUNNING);

    private final ResourceQuotaRepository quotaRepository;
    private final ResearchWorkflowStepRepository stepRepository;

    public ResourceQuotaService(
            ResourceQuotaRepository quotaRepository,
            ResearchWorkflowStepRepository stepRepository) {
        this.quotaRepository = quotaRepository;
        this.stepRepository = stepRepository;
    }

    /**
     * Enforce the project's parallel-job quota before admitting a new execution.
     *
     * Must be called inside an active transaction so that the pessimistic lock
     * (findByIdForUpdate) is held until the caller sets the step to READY and
     * saves it. This serialises admission for all workers competing on the
     * same project quota.
     *
     * @param projectId the project to check
     * @throws QuotaExceededException (HTTP 429) if the quota is exceeded
     */
    @Transactional
    public void enforceParallelJobQuota(@NotNull UUID projectId) {
        // If no quota row exists, the project is unlimited — allow execution.
        Optional<ResourceQuotaEntity> quotaOpt = quotaRepository.findByIdForUpdate(projectId);
        if (quotaOpt.isEmpty()) {
            return;
        }
        ResourceQuotaEntity quota = quotaOpt.get();
        if (quota.getMaxParallelJobs() == null) {
            // Explicit null in quota row = unlimited
            return;
        }

        long admitted = stepRepository.countAdmittedStepsByProjectId(projectId, ADMITTED_STATUSES);
        log.debug("Project {} admitted step count: {}/{}", projectId, admitted, quota.getMaxParallelJobs());

        if (admitted >= quota.getMaxParallelJobs()) {
            throw new QuotaExceededException("PARALLEL_JOBS", admitted, quota.getMaxParallelJobs());
        }
    }

    /**
     * Create or replace the quota for a project.
     * Validates that all non-null quota values are non-negative.
     *
     * @param projectId     target project
     * @param computeHours  maximum compute hours (null = unlimited)
     * @param aiTokens      maximum AI tokens (null = unlimited)
     * @param parallelJobs  maximum parallel steps (null = unlimited; must be >= 0)
     * @param storageBytes  maximum storage bytes (null = unlimited)
     * @return the saved entity
     */
    @Transactional
    public ResourceQuotaEntity setQuota(UUID projectId,
                                        Double computeHours,
                                        Long aiTokens,
                                        Integer parallelJobs,
                                        Long storageBytes) {
        if (computeHours != null && computeHours < 0) {
            throw new IllegalArgumentException("Compute quota must be non-negative");
        }
        if (aiTokens != null && aiTokens < 0) {
            throw new IllegalArgumentException("AI token quota must be non-negative");
        }
        if (parallelJobs != null && parallelJobs < 0) {
            throw new IllegalArgumentException("Parallel jobs quota must be non-negative");
        }
        if (storageBytes != null && storageBytes < 0) {
            throw new IllegalArgumentException("Storage quota must be non-negative");
        }
        ResourceQuotaEntity quota = new ResourceQuotaEntity(projectId, computeHours, aiTokens, parallelJobs, storageBytes);
        return quotaRepository.save(quota);
    }
}
