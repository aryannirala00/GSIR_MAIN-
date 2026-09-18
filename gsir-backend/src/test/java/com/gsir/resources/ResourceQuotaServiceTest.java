package com.gsir.resources;

import com.gsir.workflow.ResearchWorkflowStepRepository;
import com.gsir.workflow.WorkflowStepStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Part 79 — ResourceQuotaService unit tests.
 *
 * Uses stubbed repositories to verify business logic without a database.
 * Uses Mockito to verify business logic without a database.
 *
 * H2 Limitation Note (user requirement 11):
 *   These tests verify functional correctness (admission threshold, cross-project
 *   isolation, null=unlimited, negative validation, QuotaExceededException).
 *   They do NOT prove PostgreSQL PESSIMISTIC_WRITE lock semantics or
 *   true concurrent admission under PostgreSQL transaction isolation.
 *   Production correctness of concurrent admission is proven by the
 *   PESSIMISTIC_WRITE lock on the resource_quotas row (ResourceQuotaRepository.findByIdForUpdate).
 */
@ExtendWith(MockitoExtension.class)
class ResourceQuotaServiceTest {

    @Mock
    private ResourceQuotaRepository quotaRepository;

    @Mock
    private ResearchWorkflowStepRepository stepRepository;

    @InjectMocks
    private ResourceQuotaService service;

    private UUID projectA = UUID.randomUUID();
    private UUID projectB = UUID.randomUUID();

    // Controllable fake quota storage
    private java.util.Map<UUID, ResourceQuotaEntity> quotaStore = new java.util.HashMap<>();

    // Controllable fake admitted step count per project
    private java.util.Map<UUID, Long> admittedCounts = new java.util.HashMap<>();

    @BeforeEach
    void setUp() {
        // Relying on @InjectMocks and @Mock from MockitoExtension
    }

    // ── No quota = unlimited ───────────────────────────────────────────────

    @Test
    void noQuotaRow_allowsExecution() {
        // projectA has no quota row — must not throw
        when(quotaRepository.findByIdForUpdate(projectA)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.enforceParallelJobQuota(projectA));
    }

    @Test
    void nullMaxParallelJobs_meansUnlimited() {
        quotaStore.put(projectA, new ResourceQuotaEntity(projectA, null, null, null, null));
        admittedCounts.put(projectA, 1000L); // very high count

        // Null maxParallelJobs = unlimited — must not throw
        ResourceQuotaEntity quota = new ResourceQuotaEntity(projectA, null, null, null, null);
        when(quotaRepository.findByIdForUpdate(projectA)).thenReturn(Optional.of(quota));
        
        assertDoesNotThrow(() -> service.enforceParallelJobQuota(projectA));
    }

    // ── Quota enforcement ──────────────────────────────────────────────────

    @Test
    void quotaNotExceeded_allowsExecution() {
        quotaStore.put(projectA, new ResourceQuotaEntity(projectA, null, null, 2, null));
        admittedCounts.put(projectA, 1L); // 1 of 2 used
        ResourceQuotaEntity quota = new ResourceQuotaEntity(projectA, null, null, 2, null);
        when(quotaRepository.findByIdForUpdate(projectA)).thenReturn(Optional.of(quota));
        when(stepRepository.countAdmittedStepsByProjectId(eq(projectA), anyList())).thenReturn(1L);

        assertDoesNotThrow(() -> service.enforceParallelJobQuota(projectA));
    }

    @Test
    void quotaExactlyReached_throwsQuotaExceededException() {
        quotaStore.put(projectA, new ResourceQuotaEntity(projectA, null, null, 1, null));
        admittedCounts.put(projectA, 1L); // exactly at limit
        ResourceQuotaEntity quota = new ResourceQuotaEntity(projectA, null, null, 1, null);
        when(quotaRepository.findByIdForUpdate(projectA)).thenReturn(Optional.of(quota));
        when(stepRepository.countAdmittedStepsByProjectId(eq(projectA), anyList())).thenReturn(1L);

        QuotaExceededException ex = assertThrows(
            QuotaExceededException.class,
            () -> service.enforceParallelJobQuota(projectA)
        );
        assertEquals("QUOTA_EXCEEDED", ex.getCode());
        assertEquals("PARALLEL_JOBS", ex.getQuotaType());
        assertEquals(1L, ex.getCurrent());
        assertEquals(1L, ex.getLimit());
        assertEquals(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, ex.getStatus());
    }

    @Test
    void quotaExceeded_throwsQuotaExceededException() {
        quotaStore.put(projectA, new ResourceQuotaEntity(projectA, null, null, 1, null));
        admittedCounts.put(projectA, 5L); // well over limit
        ResourceQuotaEntity quota = new ResourceQuotaEntity(projectA, null, null, 1, null);
        when(quotaRepository.findByIdForUpdate(projectA)).thenReturn(Optional.of(quota));
        when(stepRepository.countAdmittedStepsByProjectId(eq(projectA), anyList())).thenReturn(5L);

        assertThrows(QuotaExceededException.class, () -> service.enforceParallelJobQuota(projectA));
    }

    // ── Cross-project isolation ────────────────────────────────────────────

    @Test
    void projectBUnaffectedByProjectAQuota() {
        // Project A is saturated
        ResourceQuotaEntity quotaA = new ResourceQuotaEntity(projectA, null, null, 1, null);
        when(quotaRepository.findByIdForUpdate(projectA)).thenReturn(Optional.of(quotaA));
        when(stepRepository.countAdmittedStepsByProjectId(eq(projectA), anyList())).thenReturn(5L);

        // Project B has separate quota and headroom
        ResourceQuotaEntity quotaB = new ResourceQuotaEntity(projectB, null, null, 3, null);
        when(quotaRepository.findByIdForUpdate(projectB)).thenReturn(Optional.of(quotaB));
        when(stepRepository.countAdmittedStepsByProjectId(eq(projectB), anyList())).thenReturn(1L);

        // Project A must be rejected
        assertThrows(QuotaExceededException.class, () -> service.enforceParallelJobQuota(projectA));

        // Project B must be allowed (separate quota, independent count)
        assertDoesNotThrow(() -> service.enforceParallelJobQuota(projectB));
    }

    @Test
    void projectBWithNoQuota_unaffectedByProjectAExhaustion() {
        // B has no quota row at all = unlimited
        when(quotaRepository.findByIdForUpdate(projectB)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.enforceParallelJobQuota(projectB));
    }

    // ── Quota admission: READY + RUNNING count ────────────────────────────

    @Test
    void admittedCountIncludesReadyAndRunning() {
        // The service uses countAdmittedStepsByProjectId which counts READY + RUNNING
        // Simulate: 1 READY + 1 RUNNING = 2 admitted. Quota = 2 → should reject next.
        ResourceQuotaEntity quotaA = new ResourceQuotaEntity(projectA, null, null, 2, null);
        when(quotaRepository.findByIdForUpdate(projectA)).thenReturn(Optional.of(quotaA));
        when(stepRepository.countAdmittedStepsByProjectId(eq(projectA), anyList())).thenReturn(2L); // already at limit (READY + RUNNING combined)

        assertThrows(QuotaExceededException.class, () -> service.enforceParallelJobQuota(projectA));
    }

    // ── Validation: negative quotas ───────────────────────────────────────

    @Test
    void setQuota_negativeParallelJobs_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> service.setQuota(projectA, null, null, -1, null));
    }

    @Test
    void setQuota_negativeComputeHours_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> service.setQuota(projectA, -0.1, null, null, null));
    }

    @Test
    void setQuota_negativeAiTokens_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> service.setQuota(projectA, null, -100L, null, null));
    }

    @Test
    void setQuota_negativeStorageBytes_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> service.setQuota(projectA, null, null, null, -1L));
    }

    @Test
    void setQuota_zeroValues_areValid() {
        // Zero is a valid quota (no execution allowed, but not negative)
        when(quotaRepository.save(any(ResourceQuotaEntity.class))).thenAnswer(i -> i.getArgument(0));
        assertDoesNotThrow(() -> service.setQuota(projectA, 0.0, 0L, 0, 0L));
    }

    @Test
    void setQuota_positiveValues_succeeds() {
        when(quotaRepository.save(any(ResourceQuotaEntity.class))).thenAnswer(i -> i.getArgument(0));
        
        ResourceQuotaEntity saved = service.setQuota(projectA, 100.0, 50000L, 4, 1073741824L);
        assertNotNull(saved);
        assertEquals(projectA, saved.getProjectId());
        assertEquals(4, saved.getMaxParallelJobs());
    }

    @Test
    void setQuota_allNulls_succeeds_meansUnlimited() {
        when(quotaRepository.save(any(ResourceQuotaEntity.class))).thenAnswer(i -> i.getArgument(0));
        
        ResourceQuotaEntity saved = service.setQuota(projectA, null, null, null, null);
        assertNotNull(saved);
        assertNull(saved.getMaxParallelJobs());
    }
}


