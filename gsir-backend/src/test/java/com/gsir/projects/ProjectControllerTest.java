package com.gsir.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.auth.AuthService;
import com.gsir.auth.dto.AuthResponse;
import com.gsir.auth.dto.RegisterRequest;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.organizations.OrganizationEntity;
import com.gsir.organizations.OrganizationMembershipEntity;
import com.gsir.organizations.OrganizationMembershipRepository;
import com.gsir.organizations.OrganizationRepository;
import com.gsir.projects.dto.CreateProjectRequest;
import com.gsir.users.RefreshTokenRepository;
import com.gsir.users.SessionRepository;
import com.gsir.users.UserRepository;
import com.gsir.workspaces.WorkspaceEntity;
import com.gsir.workspaces.WorkspaceMembershipEntity;
import com.gsir.workspaces.WorkspaceMembershipRepository;
import com.gsir.workspaces.WorkspaceRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository organizationMembershipRepository;
    @Autowired private WorkspaceRepository workspaceRepository;
    @Autowired private WorkspaceMembershipRepository workspaceMembershipRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UuidV7Generator idGenerator;

    private String userAToken;
    private UUID userAId;
    private WorkspaceEntity workspaceA;

    private String userBToken;
    private UUID userBId;
    private WorkspaceEntity workspaceB;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAllInBatch();
        workspaceMembershipRepository.deleteAllInBatch();
        workspaceRepository.deleteAllInBatch();
        organizationMembershipRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
        sessionRepository.deleteAllInBatch();
        refreshTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // Register User A
        AuthResponse authA = authService.register(new RegisterRequest("alice@example.com", "Password123"));
        userAToken = authA.accessToken();
        userAId = userRepository.findByEmail("alice@example.com").orElseThrow().getId();

        OrganizationEntity orgA = organizationRepository.save(new OrganizationEntity(idGenerator.generate(), "Org A", userAId));
        organizationMembershipRepository.save(new OrganizationMembershipEntity(orgA.getId(), userAId, "OWNER"));
        workspaceA = workspaceRepository.save(new WorkspaceEntity(idGenerator.generate(), orgA.getId(), "Workspace A"));
        workspaceMembershipRepository.save(new WorkspaceMembershipEntity(workspaceA.getId(), userAId, "MEMBER"));

        // Register User B
        AuthResponse authB = authService.register(new RegisterRequest("bob@example.com", "Password123"));
        userBToken = authB.accessToken();
        userBId = userRepository.findByEmail("bob@example.com").orElseThrow().getId();

        OrganizationEntity orgB = organizationRepository.save(new OrganizationEntity(idGenerator.generate(), "Org B", userBId));
        organizationMembershipRepository.save(new OrganizationMembershipEntity(orgB.getId(), userBId, "OWNER"));
        workspaceB = workspaceRepository.save(new WorkspaceEntity(idGenerator.generate(), orgB.getId(), "Workspace B"));
        workspaceMembershipRepository.save(new WorkspaceMembershipEntity(workspaceB.getId(), userBId, "MEMBER"));
    }

    @Test
    void testCreateGetAndListProjectWithApiResponseEnvelope() throws Exception {
        CreateProjectRequest createReq = new CreateProjectRequest(
                workspaceA.getId(),
                "Quantum Research",
                "Advanced quantum simulations"
        );

        // 1. Create Project
        MvcResult createResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + userAToken)
                        .header("X-Request-ID", "custom-req-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Request-ID", "custom-req-id-1"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.requestId", is("custom-req-id-1")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.data.name", is("Quantum Research")))
                .andExpect(jsonPath("$.data.workspaceId", is(workspaceA.getId().toString())))
                .andReturn();

        String projectIdStr = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");
        UUID projectId = UUID.fromString(projectIdStr);

        // 2. Get Project by ID
        mockMvc.perform(get("/api/v1/projects/" + projectId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(projectId.toString())))
                .andExpect(jsonPath("$.data.name", is("Quantum Research")));

        // 3. List Projects (PageResponse)
        mockMvc.perform(get("/api/v1/projects")
                        .header("Authorization", "Bearer " + userAToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items[0].id", is(projectId.toString())))
                .andExpect(jsonPath("$.data.content[0].id", is(projectId.toString())))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(10)))
                .andExpect(jsonPath("$.data.totalElements", is(1)))
                .andExpect(jsonPath("$.data.first", is(true)))
                .andExpect(jsonPath("$.data.last", is(true)));
    }

    @Test
    void testTenantIsolationReturns404NotFoundForCrossTenantAccess() throws Exception {
        CreateProjectRequest createReq = new CreateProjectRequest(
                workspaceA.getId(),
                "Alice Secret Project",
                "Confidential"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String projectIdStr = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");

        // User B attempts to access User A's project -> MUST return 404 RESOURCE_NOT_FOUND (not 403, preserving tenant privacy)
        mockMvc.perform(get("/api/v1/projects/" + projectIdStr)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Project was not found")))
                .andExpect(jsonPath("$.requestId", notNullValue()))
                .andExpect(jsonPath("$.fields").isEmpty());
    }
}
