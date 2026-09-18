package com.gsir.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.data.dto.RegisterDataRequest;
import com.gsir.data.model.DataObjectEntity;
import com.gsir.data.repository.DataObjectRepository;
import com.gsir.projects.ProjectEntity;
import com.gsir.projects.ProjectRepository;
import com.gsir.users.UserEntity;
import com.gsir.users.UserRepository;
import com.gsir.workspaces.WorkspaceEntity;
import com.gsir.workspaces.WorkspaceRepository;
import com.gsir.workspaces.WorkspaceMembershipEntity;
import com.gsir.workspaces.WorkspaceMembershipRepository;
import com.gsir.organizations.OrganizationEntity;
import com.gsir.organizations.OrganizationRepository;
import com.gsir.organizations.OrganizationMembershipEntity;
import com.gsir.organizations.OrganizationMembershipRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Part87DatasetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMembershipRepository organizationMembershipRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private DataObjectRepository dataObjectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity testUser;
    private ProjectEntity testProject;

    @BeforeEach
    void setUp() {
        dataObjectRepository.deleteAll();
        projectRepository.deleteAll();
        workspaceMembershipRepository.deleteAll();
        workspaceRepository.deleteAll();
        organizationMembershipRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

        UUID userId = UUID.randomUUID();
        testUser = new UserEntity(userId, "test@example.com", "password", "ACTIVE");
        userRepository.save(testUser);

        UUID orgId = UUID.randomUUID();
        OrganizationEntity org = new OrganizationEntity(orgId, "Test Org", userId);
        organizationRepository.save(org);
        
        organizationMembershipRepository.save(new OrganizationMembershipEntity(orgId, userId, "OWNER"));

        UUID workspaceId = UUID.randomUUID();
        WorkspaceEntity workspace = new WorkspaceEntity(workspaceId, orgId, "Test Workspace");
        workspaceRepository.save(workspace);

        workspaceMembershipRepository.save(new WorkspaceMembershipEntity(workspaceId, userId, "OWNER"));

        UUID projectId = UUID.randomUUID();
        testProject = new ProjectEntity(projectId, workspaceId, "Test Project", "Desc");
        projectRepository.save(testProject);
    }

    @AfterEach
    void tearDown() {
        dataObjectRepository.deleteAll();
        projectRepository.deleteAll();
        workspaceMembershipRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testDatasetLifecycle() throws Exception {
        UUID projectId = testProject.getId();
        UUID userId = testUser.getId();
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userId, null, java.util.Collections.emptyList());

        // 1. Create Dataset (Register)
        RegisterDataRequest request = new RegisterDataRequest();
        request.setName("My Dataset");
        request.setDataType("OBSERVATIONAL");
        request.setClassification("INTERNAL");

        String createResponse = mockMvc.perform(post("/api/v1/projects/{projectId}/data", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("My Dataset"))
                .andReturn().getResponse().getContentAsString();

        String dataIdStr = objectMapper.readTree(createResponse).get("data").get("id").asText();
        UUID dataId = UUID.fromString(dataIdStr);

        // 2. Update Dataset
        RegisterDataRequest updateReq = new RegisterDataRequest();
        updateReq.setVersion("v2.0");
        mockMvc.perform(put("/api/v1/projects/{projectId}/data/{dataId}", projectId, dataIdStr)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value("v2.0"));

        // 3. Archive Dataset
        mockMvc.perform(post("/api/v1/projects/{projectId}/data/{dataId}/archive", projectId, dataIdStr)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));

        // 4. Restore Dataset
        mockMvc.perform(post("/api/v1/projects/{projectId}/data/{dataId}/restore", projectId, dataIdStr)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESTORED"));

        // 5. Delete Dataset
        mockMvc.perform(delete("/api/v1/projects/{projectId}/data/{dataId}", projectId, dataIdStr)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk());

        // Verify Deletion
        assertFalse(dataObjectRepository.findById(dataId).isPresent());
    }

    @Test
    void testDatasetStubsThrowNotImplemented() throws Exception {
        UUID projectId = testProject.getId();
        UUID userId = testUser.getId();
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userId, null, java.util.Collections.emptyList());

        DataObjectEntity entity = new DataObjectEntity(
                UUID.randomUUID(), projectId, userId, "RAW", "Test", "v1.0", "INTERNAL", "REGISTERED", Instant.now());
        dataObjectRepository.save(entity);
        String dataIdStr = entity.getId().toString();

        mockMvc.perform(post("/api/v1/projects/{projectId}/data/{dataId}/validate", projectId, dataIdStr)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isNotImplemented());

        mockMvc.perform(post("/api/v1/projects/{projectId}/data/{dataId}/export", projectId, dataIdStr)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void testCrossProjectIsolation() throws Exception {
        UUID otherOrgId = UUID.randomUUID();
        OrganizationEntity otherOrg = new OrganizationEntity(otherOrgId, "Other Org", testUser.getId());
        organizationRepository.save(otherOrg);
        // Do not save org membership to ensure no access

        UUID otherWorkspaceId = UUID.randomUUID();
        WorkspaceEntity otherWorkspace = new WorkspaceEntity(otherWorkspaceId, otherOrgId, "Other Workspace");
        workspaceRepository.save(otherWorkspace);

        UUID otherProjectId = UUID.randomUUID();
        ProjectEntity otherProject = new ProjectEntity(otherProjectId, otherWorkspaceId, "Other Project", "Desc");
        projectRepository.save(otherProject);

        UUID userId = testUser.getId();
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userId, null, java.util.Collections.emptyList());

        DataObjectEntity entity = new DataObjectEntity(
                UUID.randomUUID(), otherProjectId, userId, "RAW", "Test", "v1.0", "INTERNAL", "REGISTERED", Instant.now());
        dataObjectRepository.save(entity);
        String dataIdStr = entity.getId().toString();

        // User tries to access from their project
        mockMvc.perform(get("/api/v1/projects/{projectId}/data/{dataId}", testProject.getId(), dataIdStr)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isForbidden());
    }
}
