package com.gsir.projects;

import com.gsir.common.error.GsirException;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.organizations.OrganizationEntity;
import com.gsir.organizations.OrganizationMembershipEntity;
import com.gsir.organizations.OrganizationMembershipRepository;
import com.gsir.organizations.OrganizationRepository;
import com.gsir.users.UserEntity;
import com.gsir.users.UserRepository;
import com.gsir.workspaces.WorkspaceEntity;
import com.gsir.workspaces.WorkspaceMembershipEntity;
import com.gsir.workspaces.WorkspaceMembershipRepository;
import com.gsir.workspaces.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectAccessServiceTest {

    @Autowired
    private ProjectAccessService projectAccessService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMembershipRepository organizationMembershipRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UuidV7Generator idGenerator;

    // Tenant A fixture
    private UserEntity userA;
    private OrganizationEntity orgA;
    private WorkspaceEntity workspaceA1;
    private WorkspaceEntity workspaceA2;
    private ProjectEntity projectA1;
    private ProjectEntity projectA2;

    // Tenant B fixture
    private UserEntity userB;
    private OrganizationEntity orgB;
    private WorkspaceEntity workspaceB;
    private ProjectEntity projectB;

    @BeforeEach
    void setUp() {
        // Create Tenant A hierarchy
        userA = userRepository.save(new UserEntity(idGenerator.generate(), "alice@example.com", "hash_a", "ACTIVE"));
        orgA = organizationRepository.save(new OrganizationEntity(idGenerator.generate(), "Acme Corp", userA.getId()));
        organizationMembershipRepository.save(new OrganizationMembershipEntity(orgA.getId(), userA.getId(), "OWNER"));

        workspaceA1 = workspaceRepository.save(new WorkspaceEntity(idGenerator.generate(), orgA.getId(), "Engineering"));
        workspaceMembershipRepository.save(new WorkspaceMembershipEntity(workspaceA1.getId(), userA.getId(), "MEMBER"));

        workspaceA2 = workspaceRepository.save(new WorkspaceEntity(idGenerator.generate(), orgA.getId(), "Marketing"));
        // User A is NOT a member of workspaceA2

        projectA1 = projectRepository.save(new ProjectEntity(idGenerator.generate(), workspaceA1.getId(), "Compiler Project", "Core compiler"));
        projectA2 = projectRepository.save(new ProjectEntity(idGenerator.generate(), workspaceA2.getId(), "Campaign Project", "Marketing campaign"));

        // Create Tenant B hierarchy
        userB = userRepository.save(new UserEntity(idGenerator.generate(), "bob@example.com", "hash_b", "ACTIVE"));
        orgB = organizationRepository.save(new OrganizationEntity(idGenerator.generate(), "Beta LLC", userB.getId()));
        organizationMembershipRepository.save(new OrganizationMembershipEntity(orgB.getId(), userB.getId(), "OWNER"));

        workspaceB = workspaceRepository.save(new WorkspaceEntity(idGenerator.generate(), orgB.getId(), "Beta Workspace"));
        workspaceMembershipRepository.save(new WorkspaceMembershipEntity(workspaceB.getId(), userB.getId(), "OWNER"));

        projectB = projectRepository.save(new ProjectEntity(idGenerator.generate(), workspaceB.getId(), "Beta Project", "Confidential"));
    }

    @Test
    @DisplayName("A. Valid access: authorized user -> own organization -> own workspace -> own project")
    void testValidAccess() {
        ProjectEntity project = projectAccessService.requireAccess(projectA1.getId(), userA.getId());

        assertThat(project).isNotNull();
        assertThat(project.getId()).isEqualTo(projectA1.getId());
        assertThat(project.getName()).isEqualTo("Compiler Project");
        assertThat(project.getWorkspaceId()).isEqualTo(workspaceA1.getId());

        // Full hierarchy access check
        ProjectEntity verifiedHierarchy = projectAccessService.requireAccess(orgA.getId(), workspaceA1.getId(), projectA1.getId(), userA.getId());
        assertThat(verifiedHierarchy.getId()).isEqualTo(projectA1.getId());
    }

    @Test
    @DisplayName("B. Cross-organization rejection: user from Organization A attempts Project belonging to Organization B")
    void testCrossOrganizationRejection() {
        assertThatThrownBy(() -> projectAccessService.requireAccess(projectB.getId(), userA.getId()))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });

        assertThatThrownBy(() -> projectAccessService.requireAccess(orgB.getId(), workspaceB.getId(), projectB.getId(), userA.getId()))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });
    }

    @Test
    @DisplayName("C. Cross-workspace rejection: user authorized for one workspace attempts project in another workspace without membership")
    void testCrossWorkspaceRejection() {
        // User A belongs to Org A and Workspace A1, but NOT Workspace A2
        assertThatThrownBy(() -> projectAccessService.requireAccess(projectA2.getId(), userA.getId()))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });
    }

    @Test
    @DisplayName("D. Invalid containment: project/workspace relationship does not match expected hierarchy")
    void testInvalidContainment() {
        // Project A1 belongs to Workspace A1, not Workspace B or Workspace A2
        assertThatThrownBy(() -> projectAccessService.requireAccess(orgA.getId(), workspaceA2.getId(), projectA1.getId(), userA.getId()))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });
    }

    @Test
    @DisplayName("E. Missing resource: non-existent project or organization throws RESOURCE_NOT_FOUND")
    void testMissingResource() {
        UUID nonExistentProjectId = idGenerator.generate();
        assertThatThrownBy(() -> projectAccessService.requireAccess(nonExistentProjectId, userA.getId()))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });
    }

    @Test
    @DisplayName("F. Unauthorized membership: user exists but has no required tenancy membership")
    void testUnauthorizedMembership() {
        UserEntity unassignedUser = userRepository.save(new UserEntity(idGenerator.generate(), "outsider@example.com", "hash", "ACTIVE"));

        assertThatThrownBy(() -> projectAccessService.requireAccess(projectA1.getId(), unassignedUser.getId()))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });
    }

    @Test
    @DisplayName("G. Boundary enforcement: client-supplied tenant IDs cannot bypass database relationship")
    void testBoundaryEnforcementWithSpoofedTenantIds() {
        // User A tries to claim Org B owns Project A1
        assertThatThrownBy(() -> projectAccessService.requireAccess(orgB.getId(), workspaceA1.getId(), projectA1.getId(), userA.getId()))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ge.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
                });
    }

    @Test
    @DisplayName("Input validation: null arguments throw INVALID_REQUEST")
    void testNullValidation() {
        assertThatThrownBy(() -> projectAccessService.requireAccess(null, userA.getId()))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ge.getCode()).isEqualTo("INVALID_REQUEST");
                });

        assertThatThrownBy(() -> projectAccessService.requireAccess(projectA1.getId(), null))
                .isInstanceOf(GsirException.class)
                .satisfies(e -> {
                    GsirException ge = (GsirException) e;
                    assertThat(ge.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ge.getCode()).isEqualTo("INVALID_REQUEST");
                });
    }
}

