package com.gsir.workflow;

import com.gsir.common.idempotency.IdempotencyRecordRepository;
import com.gsir.jobs.JobRepository;
import com.gsir.projects.ProjectEntity;
import com.gsir.projects.ProjectRepository;
import com.gsir.users.UserEntity;
import com.gsir.users.UserRepository;
import com.gsir.workspaces.WorkspaceEntity;
import com.gsir.workspaces.WorkspaceMembershipEntity;
import com.gsir.workspaces.WorkspaceMembershipRepository;
import com.gsir.workspaces.WorkspaceRepository;
import com.gsir.organizations.OrganizationEntity;
import com.gsir.organizations.OrganizationMembershipEntity;
import com.gsir.organizations.OrganizationMembershipRepository;
import com.gsir.organizations.OrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.gsir.GsirApplication.class)
@ActiveProfiles("test")
class WorkflowConcurrencyTest {

    @Autowired private WorkflowOrchestrator orchestrator;
    @Autowired private ResearchWorkflowRepository workflowRepository;
    @Autowired private ResearchWorkflowStepRepository stepRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private IdempotencyRecordRepository idempotencyRecordRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository orgRepo;
    @Autowired private OrganizationMembershipRepository orgMemRepo;
    @Autowired private WorkspaceRepository workspaceRepo;
    @Autowired private WorkspaceMembershipRepository workspaceMemRepo;
    @Autowired private ProjectRepository projectRepo;

    private UUID userId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        cleanDb();
        UserEntity user = userRepository.save(new UserEntity(UUID.randomUUID(), "wfconc@test.com", "hash", "ACTIVE"));
        userId = user.getId();

        OrganizationEntity org = orgRepo.save(new OrganizationEntity(UUID.randomUUID(), "Org", userId));
        orgMemRepo.save(new OrganizationMembershipEntity(org.getId(), userId, "OWNER"));

        WorkspaceEntity ws = workspaceRepo.save(new WorkspaceEntity(UUID.randomUUID(), org.getId(), "WS"));
        workspaceMemRepo.save(new WorkspaceMembershipEntity(ws.getId(), userId, "MEMBER"));

        ProjectEntity proj = projectRepo.save(new ProjectEntity(UUID.randomUUID(), ws.getId(), "Proj", "Desc"));
        projectId = proj.getId();
    }

    @AfterEach
    void tearDown() {
        cleanDb();
    }

    private void cleanDb() {
        stepRepository.deleteAll();
        workflowRepository.deleteAll();
        jobRepository.deleteAll();
        idempotencyRecordRepository.deleteAll();
        projectRepo.deleteAll();
        workspaceMemRepo.deleteAll();
        workspaceRepo.deleteAll();
        orgMemRepo.deleteAll();
        orgRepo.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testConcurrentStepCompletion() throws Exception {
        // Create workflow with A, and B, C depending on A. D depends on B, C.
        CreateWorkflowRequest.StepRequest stepA = new CreateWorkflowRequest.StepRequest("A", "CAP", "{}", List.of());
        CreateWorkflowRequest.StepRequest stepB = new CreateWorkflowRequest.StepRequest("B", "CAP", "{}", List.of("A"));
        CreateWorkflowRequest.StepRequest stepC = new CreateWorkflowRequest.StepRequest("C", "CAP", "{}", List.of("A"));
        CreateWorkflowRequest.StepRequest stepD = new CreateWorkflowRequest.StepRequest("D", "CAP", "{}", List.of("B", "C"));
        
        CreateWorkflowRequest req = new CreateWorkflowRequest("Test Concurrent DAG", List.of(stepA, stepB, stepC, stepD));
        ResearchWorkflowEntity wf = orchestrator.createWorkflow(projectId, userId, "idem-2", req);
        
        orchestrator.startWorkflow(wf.getId(), userId);
        
        List<ResearchWorkflowStepEntity> steps = stepRepository.findByWorkflowId(wf.getId());
        UUID idA = steps.stream().filter(s -> s.getStepName().equals("A")).findFirst().get().getId();
        UUID idB = steps.stream().filter(s -> s.getStepName().equals("B")).findFirst().get().getId();
        UUID idC = steps.stream().filter(s -> s.getStepName().equals("C")).findFirst().get().getId();
        UUID idD = steps.stream().filter(s -> s.getStepName().equals("D")).findFirst().get().getId();
        
        orchestrator.markStepCompleted(idA, "{}");
        
        // B and C are now READY
        
        // Simulate B and C completing concurrently
        int threadCount = 2;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        CopyOnWriteArrayList<Throwable> errors = new CopyOnWriteArrayList<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        executor.submit(() -> {
            try {
                startGate.await();
                orchestrator.markStepCompleted(idB, "{}");
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        });
        executor.submit(() -> {
            try {
                startGate.await();
                orchestrator.markStepCompleted(idC, "{}");
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        });
        
        startGate.countDown();
        doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdownNow();
        
        assertThat(errors).isEmpty();
        
        // Now D should be READY exactly once (only 1 job scheduled)
        ResearchWorkflowStepEntity entityD = stepRepository.findById(idD).get();
        assertThat(entityD.getStatus()).isEqualTo(WorkflowStepStatus.READY);
        
        // D's Job should be scheduled exactly once
        long dJobs = jobRepository.count(); 
        // 1 for A, 1 for B, 1 for C, 1 for D = 4 total jobs scheduled (since we clear Job repo before)
        assertThat(dJobs).isEqualTo(4);
    }
}
