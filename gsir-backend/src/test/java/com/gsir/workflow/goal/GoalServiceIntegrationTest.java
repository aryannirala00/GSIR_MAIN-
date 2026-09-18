package com.gsir.workflow.goal;

import com.gsir.common.error.GsirException;
import com.gsir.projects.ProjectEntity;
import com.gsir.projects.ProjectRepository;
import com.gsir.provenance.model.ProvActivity;
import com.gsir.provenance.repository.ProvenanceRepository;
import com.gsir.projects.ProjectAccessService;
import com.gsir.workflow.ResearchWorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;

@SpringBootTest(classes = {com.gsir.GsirApplication.class, GoalServiceIntegrationTest.FakeConfig.class})
@ActiveProfiles("test")
class GoalServiceIntegrationTest {

    @TestConfiguration
    static class FakeConfig {
        @Bean
        @Primary
        public ProjectAccessService fakeProjectAccessService() {
            return new ProjectAccessService(null) {
                @Override
                public ProjectEntity requireAccess(UUID projectId, UUID userId) {
                    return new ProjectEntity(projectId, UUID.randomUUID(), "Test Project", "Desc");
                }
            };
        }
        @Bean
        @Primary
        public ProvenanceRepository fakeProvenanceRepository() {
            return new ProvenanceRepository(null, null) {
                private final java.util.List<ProvActivity> activities = new java.util.concurrent.CopyOnWriteArrayList<>();

                @Override
                public ProvActivity saveActivity(ProvActivity activity) {
                    activities.add(activity);
                    return activity;
                }
                
                @Override
                public java.util.List<ProvActivity> findActivitiesByProjectId(UUID projectId) {
                    return activities.stream()
                            .filter(a -> a.projectId().equals(projectId))
                            .toList();
                }
            };
        }
    }

    @Autowired
    private GoalService goalService;

    @Autowired
    private ResearchWorkflowRepository workflowRepository;

    @Autowired
    private ProvenanceRepository provenanceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAccessService projectAccessService;

    private UUID projectId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        workflowRepository.deleteAll();
        
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        
        // Setup a real project for pessimistic locking to work
        ProjectEntity project = new ProjectEntity(projectId, UUID.randomUUID(), "Test Project", "Desc");
        projectRepository.save(project);
    }

    @Test
    @Transactional
    void testEstablishGoal_Success() {
        Goal goal = goalService.establishGoal(projectId, userId, "Solve P=NP", 1.0);
        assertNotNull(goal.id());
        assertEquals("Solve P=NP", goal.description());
        assertEquals(GoalStatus.ACTIVE, goal.status());

        // Part 84.9 Provenance Verification
        List<ProvActivity> activities = provenanceRepository.findActivitiesByProjectId(projectId);
        assertEquals(1, activities.size());
        assertEquals("GOAL_ESTABLISHMENT", activities.get(0).activityType());
        assertTrue(activities.get(0).parameters().containsValue(goal.id().toString()));
    }

    @Test
    void testEstablishGoal_ConcurrencyLimit() throws InterruptedException {
        // Part 84.5 Concurrency Test
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    goalService.establishGoal(projectId, userId, "Concurrent Goal", 1.0);
                    successCount.incrementAndGet();
                } catch (GsirException e) {
                    if ("CONFLICT".equals(e.getCode())) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // unexpected
                } finally {
                    endLatch.countDown();
                }
            }));
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // Due to the pessimistic lock on ProjectRepository, we are guaranteed exact serialization.
        // The policy states max 5 active goals.
        assertEquals(5, successCount.get(), "Exactly 5 goals should be successfully created");
        assertEquals(5, conflictCount.get(), "Exactly 5 requests should be rejected with CONFLICT");
        
        long actualGoals = workflowRepository.findAll().stream()
                .filter(w -> w.getProjectId().equals(projectId))
                .count();
        assertEquals(5, actualGoals, "Database state cannot exceed 5 active goals");
    }
}
