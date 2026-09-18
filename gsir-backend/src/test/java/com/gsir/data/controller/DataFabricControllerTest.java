package com.gsir.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.projects.ProjectAccessService;
import com.gsir.data.dto.RegisterDataRequest;
import com.gsir.data.model.DataObjectEntity;
import com.gsir.data.service.DataFabricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DataFabricControllerTest {

    private MockMvc mockMvc;
    private DataFabricService dataFabricService;
    private ProjectAccessService projectAccessService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        dataFabricService = new DataFabricService(null, null) {
            @Override
            public DataObjectEntity registerData(UUID projectId, UUID ownerId, RegisterDataRequest request) {
                DataObjectEntity entity = new DataObjectEntity();
                entity.setId(UUID.randomUUID());
                entity.setName(request.getName());
                entity.setProjectId(projectId);
                return entity;
            }
            
            @Override
            public Page<DataObjectEntity> listData(UUID projectId, Pageable pageable) {
                DataObjectEntity entity = new DataObjectEntity();
                entity.setId(UUID.randomUUID());
                entity.setProjectId(projectId);
                return new PageImpl<>(List.of(entity), org.springframework.data.domain.PageRequest.of(0, 10), 1);
            }
            
            @Override
            public DataObjectEntity getData(UUID projectId, UUID dataId) {
                DataObjectEntity entity = new DataObjectEntity();
                entity.setId(dataId);
                entity.setProjectId(projectId);
                return entity;
            }
            
            @Override
            public DataObjectEntity classifyData(UUID projectId, UUID dataId, String newClassification) {
                DataObjectEntity entity = new DataObjectEntity();
                entity.setId(dataId);
                entity.setProjectId(projectId);
                entity.setClassification(newClassification);
                return entity;
            }
        };

        projectAccessService = new ProjectAccessService(null) {
            @Override
            public com.gsir.projects.ProjectEntity requireAccess(UUID projectId, UUID userId) {
                return null;
            }
        };
        
        DataFabricController controller = new DataFabricController(dataFabricService, projectAccessService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();
        
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
    }

    @Test
    void registerData_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        RegisterDataRequest request = new RegisterDataRequest();
        request.setName("Dataset");
        request.setDataType("TABULAR");
        request.setClassification("INTERNAL");

        mockMvc.perform(post("/api/v1/projects/{projectId}/data", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Dataset"));
    }

    @Test
    void listData_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/v1/projects/{projectId}/data", projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].projectId").value(projectId.toString()));
    }

    @Test
    void getData_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID dataId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/projects/{projectId}/data/{dataId}", projectId, dataId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(dataId.toString()));
    }

    @Test
    void classifyData_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID dataId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/projects/{projectId}/data/{dataId}/classification", projectId, dataId)
                .param("classification", "CONFIDENTIAL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.classification").value("CONFIDENTIAL"));
    }
}
