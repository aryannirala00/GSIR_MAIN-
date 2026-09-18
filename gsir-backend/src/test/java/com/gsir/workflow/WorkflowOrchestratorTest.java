package com.gsir.workflow;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.gsir.GsirApplication.class)
@ActiveProfiles("test")
class WorkflowOrchestratorTest {

    @Autowired private WorkflowOrchestrator orchestrator;
    @Autowired private ResearchWorkflowRepository workflowRepository;
    @Autowired private ResearchWorkflowStepRepository stepRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository orgRepo;
    @Autowired private OrganizationMembershipRepository orgMemRepo;
    @Autowired private WorkspaceRepository workspaceRepo;
    @Autowired private WorkspaceMembershipRepository workspaceMemRepo;
    @Autowired private ProjectRepository projectRepo;
    @Autowired private ApplicationEventPublisher eventPublisher;

    private UUID userId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        cleanDb();
        UserEntity user = userRepository.save(new UserEntity(UUID.randomUUID(), "wf@test.com", "hash", "ACTIVE"));
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
        projectRepo.deleteAll();
        workspaceMemRepo.deleteAll();
        workspaceRepo.deleteAll();
        orgMemRepo.deleteAll();
        orgRepo.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testWorkflowDAGExecution() {
        // Create workflow with 3 steps: A, B(depends on A), C(depends on A)
        CreateWorkflowRequest.StepRequest stepA = new CreateWorkflowRequest.StepRequest("StepA", "LITERATURE_SEARCH", "{}", List.of());
        CreateWorkflowRequest.StepRequest stepB = new CreateWorkflowRequest.StepRequest("StepB", "SUMMARIZE", "{}", List.of("StepA"));
        CreateWorkflowRequest.StepRequest stepC = new CreateWorkflowRequest.StepRequest("StepC", "SUMMARIZE", "{}", List.of("StepA"));
        
        CreateWorkflowRequest req = new CreateWorkflowRequest("Test DAG", List.of(stepA, stepB, stepC));
        
        ResearchWorkflowEntity wf = orchestrator.createWorkflow(projectId, userId, "idem-1", req);
        
        // Start workflow
        orchestrator.startWorkflow(wf.getId(), userId);
        
        // Assert StepA is READY and scheduled as Job
        List<ResearchWorkflowStepEntity> steps = stepRepository.findByWorkflowId(wf.getId());
        assertThat(steps).hasSize(3);
        
        ResearchWorkflowStepEntity entityA = steps.stream().filter(s -> s.getStepName().equals("StepA")).findFirst().get();
        ResearchWorkflowStepEntity entityB = steps.stream().filter(s -> s.getStepName().equals("StepB")).findFirst().get();
        ResearchWorkflowStepEntity entityC = steps.stream().filter(s -> s.getStepName().equals("StepC")).findFirst().get();
        
        assertThat(entityA.getStatus()).isEqualTo(WorkflowStepStatus.READY);
        assertThat(entityA.getJobId()).isNotNull();
        assertThat(entityB.getStatus()).isEqualTo(WorkflowStepStatus.PENDING); // blocked by A
        assertThat(entityC.getStatus()).isEqualTo(WorkflowStepStatus.PENDING); // blocked by A
        
        // Simulate Job execution for A
        orchestrator.markStepCompleted(entityA.getId(), "{}");
        
        // Now B and C should be READY
        entityB = stepRepository.findById(entityB.getId()).get();
        entityC = stepRepository.findById(entityC.getId()).get();
        
        assertThat(entityB.getStatus()).isEqualTo(WorkflowStepStatus.READY);
        assertThat(entityB.getJobId()).isNotNull();
        assertThat(entityC.getStatus()).isEqualTo(WorkflowStepStatus.READY);
        assertThat(entityC.getJobId()).isNotNull();
        
        // Complete B and C
        orchestrator.markStepCompleted(entityB.getId(), "{}");
        orchestrator.markStepCompleted(entityC.getId(), "{}");
        
        // Workflow should be completed
        wf = workflowRepository.findById(wf.getId()).get();
        assertThat(wf.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
    }
}
