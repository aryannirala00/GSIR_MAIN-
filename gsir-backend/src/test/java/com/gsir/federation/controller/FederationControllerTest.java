package com.gsir.federation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.api.ApiResponse;
import com.gsir.federation.dto.RegisterSiteRequest;
import com.gsir.federation.dto.SubmitRemoteExecutionRequest;
import com.gsir.federation.model.FederationSiteEntity;
import com.gsir.federation.model.RemoteExecutionEntity;
import com.gsir.federation.service.FederationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FederationControllerTest {

    private MockMvc mockMvc;
    private FederationService federationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(UUID.randomUUID(), null, List.of())
        );

        federationService = new FederationService(null, null, null, null) {
            @Override
            public FederationSiteEntity registerSite(RegisterSiteRequest request) {
                FederationSiteEntity site = new FederationSiteEntity();
                site.setId(UUID.randomUUID());
                site.setName(request.getName());
                site.setStatus("ACTIVE");
                return site;
            }

            @Override
            public List<FederationSiteEntity> listSites() {
                FederationSiteEntity site = new FederationSiteEntity();
                site.setId(UUID.randomUUID());
                return List.of(site);
            }

            @Override
            public FederationSiteEntity getSite(UUID siteId) {
                FederationSiteEntity site = new FederationSiteEntity();
                site.setId(siteId);
                return site;
            }

            @Override
            public RemoteExecutionEntity submitRemoteExecution(UUID projectId, UUID userId, UUID siteId, SubmitRemoteExecutionRequest request) {
                RemoteExecutionEntity exec = new RemoteExecutionEntity();
                exec.setId(UUID.randomUUID());
                exec.setTargetSiteId(siteId);
                exec.setStatus("SUBMITTED");
                return exec;
            }

            @Override
            public RemoteExecutionEntity getRemoteExecution(UUID projectId, UUID userId, UUID executionId) {
                RemoteExecutionEntity exec = new RemoteExecutionEntity();
                exec.setId(executionId);
                return exec;
            }
        };

        FederationController controller = new FederationController(federationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void registerSite_Success() throws Exception {
        RegisterSiteRequest request = new RegisterSiteRequest();
        request.setName("Site A");
        request.setApiUrl("http://site-a.com");

        mockMvc.perform(post("/api/v1/federation/sites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Site A"));
    }

    @Test
    void listSites_Success() throws Exception {
        mockMvc.perform(get("/api/v1/federation/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").exists());
    }

    @Test
    void submitRemoteExecution_Success() throws Exception {
        UUID siteId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        SubmitRemoteExecutionRequest req = new SubmitRemoteExecutionRequest();
        req.setWorkflowId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/federation/projects/{projectId}/sites/{siteId}/executions", projectId, siteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }
}
