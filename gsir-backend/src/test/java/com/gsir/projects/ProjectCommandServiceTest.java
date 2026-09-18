package com.gsir.projects;

import com.gsir.common.error.GsirException;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.common.idempotency.IdempotencyRecordRepository;
import com.gsir.outbox.OutboxEvent;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ProjectCommandServiceTest {

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

    private UserEntity userA;
    private OrganizationEntity orgA;
    private WorkspaceEntity workspaceA1;
    private WorkspaceEntity workspaceA2;
    private UserEntity userB;
    private OrganizationEntity orgB;
    private WorkspaceEntity workspaceB;
    private UserEntity unassignedUser;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(new UserEntity(idGenerator.generate(), "alice@example.com", "hash", "ACTIVE"));
        orgA = organizationRepository.save(new OrganizationEntity(idGenerator.generate(), "Acme Corp", userA.getId()));
        organizationMembershipRepository.save(new OrganizationMembershipEntity(orgA.getId(), userA.getId(), "OWNER"));

        workspaceA1 = workspaceRepository.save(new WorkspaceEntity(idGenerator.generate(), orgA.getId(), "Engineering"));
        workspaceMembershipRepository.save(new WorkspaceMembershipEntity(workspaceA1.getId(), userA.getId(), "MEMBER"));

        workspaceA2 = workspaceRepository.save(new WorkspaceEntity(idGenerator.generate(), orgA.getId(), "Marketing"));

        userB = userRepository.save(new UserEntity(idGenerator.generate(), "bob@example.com", "hash", "ACTIVE"));
        orgB = organizationRepository.save(new OrganizationEntity(idGenerator.generate(), "Beta LLC", userB.getId()));
        organizationMembershipRepository.save(new OrganizationMembershipEntity(orgB.getId(), userB.getId(), "OWNER"));

        workspaceB = workspaceRepository.save(new WorkspaceEntity(idGenerator.generate(), orgB.getId(), "Beta Workspace"));
        workspaceMembershipRepository.save(new WorkspaceMembershipEntity(workspaceB.getId(), userB.getId(), "OWNER"));

        unassignedUser = userRepository.save(new UserEntity(idGenerator.generate(), "nobody@example.com", "hash", "ACTIVE"));
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
    @DisplayName("Authorized user can create a project in their workspace")
    void testAuthorizedCreation() {
        CreateProjectCommand command = new CreateProjectCommand(workspaceA1.getId(), "Genome Sequencer", "Desc");
        ProjectEntity project = projectCommandService.createProject(userA.getId(), null, command);

        assertThat(project).isNotNull();
        assertThat(project.getWorkspaceId()).isEqualTo(workspaceA1.getId());

        List<OutboxEvent> unpublished = outboxEventRepository.findUnpublished();
        assertThat(unpublished).hasSize(1);
        assertThat(unpublished.get(0).getAggregateId()).isEqualTo(project.getId());
    }

    @Test
    @DisplayName("User from Organization A cannot create a project in Organization B's workspace")
    void testCrossOrganizationRejection() {
        CreateProjectCommand command = new CreateProjectCommand(workspaceB.getId(), "Malicious Project", "Desc");

        assertThatThrownBy(() -> projectCommandService.createProject(userA.getId(), null, command))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });

        assertThat(projectRepository.count()).isZero();
        assertThat(outboxEventRepository.count()).isZero();
    }

    @Test
    @DisplayName("User belonging to Workspace A cannot create a project in Workspace B")
    void testCrossWorkspaceRejection() {
        CreateProjectCommand command = new CreateProjectCommand(workspaceA2.getId(), "Unauthorized Project", "Desc");

        assertThatThrownBy(() -> projectCommandService.createProject(userA.getId(), null, command))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });

        assertThat(projectRepository.count()).isZero();
    }

    @Test
    @DisplayName("Existing user with no membership in the target workspace is rejected")
    void testUnassignedUserRejection() {
        CreateProjectCommand command = new CreateProjectCommand(workspaceA1.getId(), "Unauthorized", "Desc");

        assertThatThrownBy(() -> projectCommandService.createProject(unassignedUser.getId(), null, command))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });
    }

    @Test
    @DisplayName("Idempotency: First request succeeds and repeated request returns previous result without duplicate mutation")
    void testIdempotencySuccessAndReplay() {
        String key = "idemp-key-123";
        CreateProjectCommand command = new CreateProjectCommand(workspaceA1.getId(), "Idempotent Project", "Desc");

        ProjectEntity project1 = projectCommandService.createProject(userA.getId(), key, command);
        assertThat(project1).isNotNull();
        long projectCount = projectRepository.count();
        long outboxCount = outboxEventRepository.count();

        ProjectEntity project2 = projectCommandService.createProject(userA.getId(), key, command);

        assertThat(project2.getId()).isEqualTo(project1.getId());
        assertThat(projectRepository.count()).isEqualTo(projectCount);
        assertThat(outboxEventRepository.count()).isEqualTo(outboxCount);
        assertThat(idempotencyRecordRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Idempotency: Different keys create independent operations")
    void testIdempotencyDifferentKeys() {
        CreateProjectCommand command = new CreateProjectCommand(workspaceA1.getId(), "Project", "Desc");

        ProjectEntity p1 = projectCommandService.createProject(userA.getId(), "key-A", command);
        ProjectEntity p2 = projectCommandService.createProject(userA.getId(), "key-B", command);

        assertThat(p1.getId()).isNotEqualTo(p2.getId());
        assertThat(projectRepository.count()).isEqualTo(2);
        assertThat(idempotencyRecordRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Idempotency: Cannot cross user authorization boundaries")
    void testIdempotencyCrossUserBoundary() {
        String key = "shared-key";
        CreateProjectCommand command1 = new CreateProjectCommand(workspaceA1.getId(), "Project A", "Desc");
        ProjectEntity p1 = projectCommandService.createProject(userA.getId(), key, command1);

        CreateProjectCommand command2 = new CreateProjectCommand(workspaceB.getId(), "Project B", "Desc");
        ProjectEntity p2 = projectCommandService.createProject(userB.getId(), key, command2);

        assertThat(p2.getId()).isNotEqualTo(p1.getId());
        assertThat(projectRepository.count()).isEqualTo(2);
        assertThat(idempotencyRecordRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Idempotency: Failed operations follow retry behavior")
    void testIdempotencyFailedOperationRetry() {
        String key = "fail-retry-key";
        CreateProjectCommand badCommand = new CreateProjectCommand(workspaceA2.getId(), "Unauthorized", "Desc");

        assertThatThrownBy(() -> projectCommandService.createProject(userA.getId(), key, badCommand))
                .isInstanceOf(GsirException.class);

        assertThat(projectRepository.count()).isZero();
        assertThat(idempotencyRecordRepository.count()).isZero();

        CreateProjectCommand goodCommand = new CreateProjectCommand(workspaceA1.getId(), "Authorized", "Desc");
        ProjectEntity project = projectCommandService.createProject(userA.getId(), key, goodCommand);

        assertThat(project).isNotNull();
        assertThat(projectRepository.count()).isEqualTo(1);
        assertThat(idempotencyRecordRepository.count()).isEqualTo(1);
    }
}
