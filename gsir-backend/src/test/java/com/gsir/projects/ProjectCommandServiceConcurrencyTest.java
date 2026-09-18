package com.gsir.projects;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.common.idempotency.IdempotencyRecordRepository;
import com.gsir.outbox.OutboxEventRepository;
import com.gsir.workspaces.WorkspaceEntity;
import com.gsir.workspaces.WorkspaceMembershipEntity;
import com.gsir.workspaces.WorkspaceMembershipRepository;
import com.gsir.workspaces.WorkspaceRepository;
import com.gsir.organizations.OrganizationEntity;
import com.gsir.organizations.OrganizationMembershipEntity;
import com.gsir.organizations.OrganizationMembershipRepository;
import com.gsir.organizations.OrganizationRepository;
import com.gsir.users.UserEntity;
import com.gsir.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real concurrency integration test for Part 14 idempotency.
 *
 * This class deliberately does NOT use class-level @Transactional so that:
 *   1. setUp() data is committed and visible to all threads.
 *   2. Each service call commits its own transaction — required for real concurrency.
 *   3. @AfterEach cleans up committed data explicitly.
 *
 * The concurrent test proves that when two threads race with the same
 * (userId, idempotencyKey), exactly one project is committed and both
 * callers receive the same result — no 409, no duplicates.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProjectCommandServiceConcurrencyTest {

    @Autowired private ProjectCommandService projectCommandService;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private OutboxEventRepository outboxEventRepository;
    @Autowired private IdempotencyRecordRepository idempotencyRecordRepository;
    @Autowired private WorkspaceRepository workspaceRepository;
    @Autowired private WorkspaceMembershipRepository workspaceMembershipRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository organizationMembershipRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UuidV7Generator idGenerator;

    private UUID userId;
    private UUID workspaceId;

    @BeforeEach
    void setUp() {
        // Each save is auto-committed by Spring Data JPA's own @Transactional.
        // The committed rows are visible to the spawned threads in the concurrent test.
        UserEntity user = userRepository.save(
                new UserEntity(idGenerator.generate(), "concurrent-user@example.com", "hash", "ACTIVE"));
        userId = user.getId();

        OrganizationEntity org = organizationRepository.save(
                new OrganizationEntity(idGenerator.generate(), "Concurrent Org", userId));
        organizationMembershipRepository.save(
                new OrganizationMembershipEntity(org.getId(), userId, "OWNER"));

        WorkspaceEntity workspace = workspaceRepository.save(
                new WorkspaceEntity(idGenerator.generate(), org.getId(), "Concurrent Workspace"));
        workspaceId = workspace.getId();
        workspaceMembershipRepository.save(
                new WorkspaceMembershipEntity(workspaceId, userId, "MEMBER"));
    }

    @AfterEach
    void tearDown() {
        idempotencyRecordRepository.deleteAll();
        outboxEventRepository.deleteAll();
        projectRepository.deleteAll();
        workspaceMembershipRepository.deleteAll();
        organizationMembershipRepository.deleteAll();
        workspaceRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Concurrent: Two simultaneous requests with same idempotency key — both return same result, no duplicates, no 409")
    void testConcurrentIdempotencyRace() throws Exception {
        final String key = UUID.randomUUID().toString();  // unique per run
        final CreateProjectCommand command = new CreateProjectCommand(workspaceId, "Concurrent Project", "Desc");
        final int threadCount = 2;

        // startGate holds both threads until all are ready, maximising the chance of a real race
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(threadCount);

        CopyOnWriteArrayList<UUID>      resultIds = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Throwable> errors    = new CopyOnWriteArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();   // wait until all threads are ready before racing
                    ProjectEntity result = projectCommandService.createProject(userId, key, command);
                    resultIds.add(result.getId());
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGate.countDown();                          // release all threads simultaneously
        boolean completed = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdownNow();

        // ── Assertions ────────────────────────────────────────────────────────────

        assertThat(completed)
                .as("All threads must complete within the timeout")
                .isTrue();

        assertThat(errors)
                .as("No exceptions should escape — concurrent duplicate must NOT return 409")
                .isEmpty();

        assertThat(resultIds)
                .as("All callers must receive a result")
                .hasSize(threadCount);

        // Both threads must return the EXACT SAME project ID
        UUID firstId = resultIds.get(0);
        assertThat(resultIds)
                .as("All callers must receive the same project ID (idempotent replay)")
                .allMatch(id -> id.equals(firstId));

        // Database must contain exactly 1 of each — no duplicates
        assertThat(projectRepository.count())
                .as("Exactly one project must be committed")
                .isEqualTo(1);

        assertThat(outboxEventRepository.count())
                .as("Exactly one outbox event must be committed")
                .isEqualTo(1);

        assertThat(idempotencyRecordRepository.count())
                .as("Exactly one idempotency record must be committed")
                .isEqualTo(1);
    }
}

