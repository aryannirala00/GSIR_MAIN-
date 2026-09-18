package com.gsir.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.data.model.DataObjectEntity;
import com.gsir.data.repository.DataObjectRepository;
import com.gsir.organizations.OrganizationEntity;
import com.gsir.organizations.OrganizationMembershipEntity;
import com.gsir.organizations.OrganizationMembershipRepository;
import com.gsir.organizations.OrganizationRepository;
import com.gsir.projects.ProjectEntity;
import com.gsir.projects.ProjectRepository;
import com.gsir.users.UserEntity;
import com.gsir.users.UserRepository;
import com.gsir.workspaces.WorkspaceEntity;
import com.gsir.workspaces.WorkspaceMembershipEntity;
import com.gsir.workspaces.WorkspaceMembershipRepository;
import com.gsir.workspaces.WorkspaceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Part88DataGovernanceTest {

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
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DataObjectRepository dataObjectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity testUser;
    private ProjectEntity testProject;
    private DataObjectEntity testData;
    private org.springframework.security.core.Authentication auth;

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
        testUser = new UserEntity(userId, "governance@example.com", "password", "ACTIVE");
        userRepository.save(testUser);

        UUID orgId = UUID.randomUUID();
        OrganizationEntity org = new OrganizationEntity(orgId, "Gov Org", userId);
        organizationRepository.save(org);
        organizationMembershipRepository.save(new OrganizationMembershipEntity(orgId, userId, "OWNER"));

        UUID workspaceId = UUID.randomUUID();
        WorkspaceEntity workspace = new WorkspaceEntity(workspaceId, orgId, "Gov Workspace");
        workspaceRepository.save(workspace);
        workspaceMembershipRepository.save(new WorkspaceMembershipEntity(workspaceId, userId, "OWNER"));

        UUID projectId = UUID.randomUUID();
        testProject = new ProjectEntity(projectId, workspaceId, "Gov Project", "Desc");
        projectRepository.save(testProject);

        testData = new DataObjectEntity(
                UUID.randomUUID(), projectId, userId, "RAW", "Gov Data", "v1.0", "INTERNAL", "REGISTERED", Instant.now());
        testData.setMetadata(java.util.Collections.singletonMap("key", "value"));
        dataObjectRepository.save(testData);

        auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        dataObjectRepository.deleteAll();
        projectRepository.deleteAll();
        workspaceMembershipRepository.deleteAll();
        workspaceRepository.deleteAll();
        organizationMembershipRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testGetMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/data/{dataId}/metadata", testProject.getId(), testData.getId())
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.key").value("value"));
    }

    @Test
    void testStubbedGovernanceEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/data/{dataId}/quality", testProject.getId(), testData.getId())
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isNotImplemented());

        mockMvc.perform(get("/api/v1/projects/{projectId}/data/search?query=test", testProject.getId())
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isNotImplemented());

        mockMvc.perform(get("/api/v1/projects/{projectId}/data/{dataId}/provenance", testProject.getId(), testData.getId())
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isNotImplemented());
                
        mockMvc.perform(post("/api/v1/projects/{projectId}/data/{dataId}/mask", testProject.getId(), testData.getId())
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isNotImplemented());
    }
}
