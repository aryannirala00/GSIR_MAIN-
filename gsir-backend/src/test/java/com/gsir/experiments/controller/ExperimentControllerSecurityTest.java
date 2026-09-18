package com.gsir.experiments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.error.GlobalExceptionHandler;
import com.gsir.common.error.GsirException;
import com.gsir.experiments.model.ExperimentEntity;
import com.gsir.experiments.service.ExperimentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExperimentControllerSecurityTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID testProjectId;

    @BeforeEach
    void setUp() {
        testProjectId = UUID.randomUUID();
        ExperimentService experimentService = new ExperimentService() {
            @Override
            public ExperimentEntity createExperiment(UUID projectId, UUID userId, String name, String description, String researchQuestion, UUID hypothesisId) {
                if (projectId.equals(testProjectId)) {
                    throw new GsirException("FORBIDDEN", "Forbidden access", HttpStatus.FORBIDDEN);
                }
                return null;
            }

            @Override
            public ExperimentEntity getExperiment(UUID experimentId, UUID projectId, UUID userId) {
                if (projectId.equals(testProjectId)) {
                    throw new GsirException("FORBIDDEN", "Forbidden access", HttpStatus.FORBIDDEN);
                }
                return null;
            }

            @Override
            public List<ExperimentEntity> listExperiments(UUID projectId, UUID userId) {
                if (projectId.equals(testProjectId)) {
                    throw new GsirException("FORBIDDEN", "Forbidden access", HttpStatus.FORBIDDEN);
                }
                return List.of();
            }

            @Override
            public ExperimentEntity updateExperimentStatus(UUID experimentId, UUID projectId, UUID userId, String status) {
                if (projectId.equals(testProjectId)) {
                    throw new GsirException("FORBIDDEN", "Forbidden access", HttpStatus.FORBIDDEN);
                }
                return null;
            }

            @Override
            public com.gsir.experiments.model.ExperimentProtocolEntity createProtocol(UUID projectId, UUID experimentId, UUID userId, String preparation, String steps, String safetyRequirements) {
                return null;
            }

            @Override
            public com.gsir.experiments.model.ExperimentRunEntity createRun(UUID projectId, UUID experimentId, UUID userId, UUID protocolId) {
                return null;
            }

            @Override
            public com.gsir.experiments.model.ExperimentObservationEntity recordObservation(UUID projectId, UUID experimentId, UUID runId, UUID userId, UUID variableId, String value, String unit, String source, Double uncertainty, String uncertaintyType, String method, String assumptions, String covarianceData) {
                return null;
            }

            @Override
            public com.gsir.experiments.model.ExperimentResultEntity recordResult(UUID projectId, UUID experimentId, UUID runId, UUID userId, String resultType, String value, String unit, Double confidence, Double uncertainty, String uncertaintyType, String method, String assumptions, String covarianceData) {
                return null;
            }
        };

        ExperimentController controller = new ExperimentController(experimentService);
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateExperiment_Unauthorized() throws Exception {
        ExperimentController.CreateExperimentRequest request = new ExperimentController.CreateExperimentRequest(
                "Test Exp", "Desc", "Q?", null
        );

        mockMvc.perform(post("/api/v1/projects/" + testProjectId + "/experiments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void testListExperiments_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + testProjectId + "/experiments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}

