package com.gsir.provenance.controller;

import com.gsir.assurance.service.ArtifactAssuranceService;
import com.gsir.common.api.ApiResponse;
import com.gsir.provenance.repository.ProvenanceRepository;
import com.gsir.provenance.service.ProvenanceService;
import com.gsir.provenance.service.ReproducibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProvenanceControllerTest {

    private MockMvc mockMvc;

    private boolean fakeValidateResult = true;
    private String expectedHash = "abc123hash";
    private boolean fakeVerifyResult = true;

    @BeforeEach
    void setUp() {
        ProvenanceService fakeProvenanceService = new ProvenanceService((com.gsir.provenance.repository.ProvenanceRepository) null) {
            @Override
            public Map<String, Object> exportProvenance(UUID projectId) {
                return Map.of("projectId", projectId, "relationships", List.of());
            }

            @Override
            public boolean validateProvenance(UUID projectId) {
                return fakeValidateResult;
            }
            
            @Override
            public Map<String, Object> compareProvenance(UUID pId, UUID sId, UUID tId) {
                return Map.of("projectId", pId);
            }
            
            @Override
            public List<com.gsir.provenance.model.ProvRelationship> getImpactAnalysis(UUID sourceId, UUID pId, int depth) {
                return List.of();
            }
            
            @Override
            public com.gsir.provenance.model.ProvArtifact registerArtifact(String type, String hash, int version, String uri, Map<String, Object> metadata, UUID pId) {
                return new com.gsir.provenance.model.ProvArtifact(UUID.randomUUID(), pId, type, hash, version, uri, metadata, java.time.Instant.now());
            }
        };

        ReproducibilityService fakeReproducibilityService = new ReproducibilityService((ProvenanceService) null, (com.gsir.workflow.WorkflowOrchestrator) null) {
            @Override
            public UUID replayActivity(UUID activityId, UUID projectId, Map<String, Object> overrides) {
                return UUID.randomUUID();
            }
        };

        ArtifactAssuranceService fakeAssuranceService = new ArtifactAssuranceService(
                (com.gsir.assurance.adapter.ResearchArtifactRepository) null,
                (com.gsir.assurance.adapter.AssuranceAttestationRepository) null,
                (com.gsir.common.id.UuidV7Generator) null,
                (com.gsir.governance.service.AuditLogService) null) {
            @Override
            public boolean verifyArtifactHash(UUID projectId, UUID artifactId, UUID actorId, String actualHash) {
                return expectedHash.equals(actualHash) && fakeVerifyResult;
            }
            
            @Override
            public com.gsir.assurance.model.ResearchArtifactEntity registerArtifact(UUID pId, UUID actorId, String type, String loc, String h, String algo) {
                return new com.gsir.assurance.model.ResearchArtifactEntity(UUID.randomUUID(), pId, type, loc, h, algo);
            }
        };

        ProvenanceController controller = new ProvenanceController(fakeProvenanceService, fakeReproducibilityService, fakeAssuranceService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testExportProvenance_Success() throws Exception {
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/projects/{projectId}/provenance/export", projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectId").value(projectId.toString()));
    }

    @Test
    void testValidateProvenance_ValidGraph() throws Exception {
        UUID projectId = UUID.randomUUID();
        fakeValidateResult = true;

        mockMvc.perform(post("/api/v1/projects/{projectId}/provenance/validate", projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testValidateProvenance_InvalidGraph() throws Exception {
        UUID projectId = UUID.randomUUID();
        fakeValidateResult = false;

        mockMvc.perform(post("/api/v1/projects/{projectId}/provenance/validate", projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void testCheckArtifactIntegrity_Verified() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();
        fakeVerifyResult = true;

        mockMvc.perform(get("/api/v1/projects/{projectId}/provenance/integrity/{artifactId}", projectId, artifactId)
                .param("actualHash", expectedHash)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));
    }

    @Test
    void testCompareProvenance() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/projects/{projectId}/provenance/compare", projectId)
                .param("sourceId", sourceId.toString())
                .param("targetId", targetId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectId").value(projectId.toString()));
    }

    @Test
    void testGetImpactAnalysis() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/projects/{projectId}/provenance/impact/{artifactId}", projectId, artifactId)
                .param("depth", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateResearchObject_Valid() throws Exception {
        UUID projectId = UUID.randomUUID();
        String payload = "{\"objectType\":\"DATASET\", \"content\":\"test content\"}";

        mockMvc.perform(post("/api/v1/projects/{projectId}/provenance/research-objects", projectId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REGISTERED"))
                .andExpect(jsonPath("$.data.hash").exists());
    }

    @Test
    void testCreateResearchObject_InvalidMissingContent() throws Exception {
        UUID projectId = UUID.randomUUID();
        String payload = "{\"objectType\":\"DATASET\"}"; // Missing content

        try {
            mockMvc.perform(post("/api/v1/projects/{projectId}/provenance/research-objects", projectId)
                    .content(payload)
                    .contentType(MediaType.APPLICATION_JSON));
            org.junit.jupiter.api.Assertions.fail("Expected exception due to missing content");
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.assertTrue(e.getCause() instanceof com.gsir.common.error.GsirException || e instanceof com.gsir.common.error.GsirException);
        }
    }
}
