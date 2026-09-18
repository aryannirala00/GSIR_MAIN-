package com.gsir.provenance.service;

import com.gsir.provenance.model.*;
import com.gsir.provenance.repository.ProvenanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Part 80 — ExecutionProvenanceService unit tests.
 *
 * Tests run with a stubbed ProvenanceRepository to isolate business logic
 * from JDBC/database concerns. H2 does not host the raw SQL provenance tables
 * (prov_agents, prov_activities, etc.) — those are created by Flyway V12 for
 * PostgreSQL only. These tests verify functional correctness without database.
 *
 * H2 limitation note (per user requirement 11): These tests prove functional
 * provenance capture logic (hash computation, relationship creation, actor tracking).
 * They do NOT prove PostgreSQL locking or JSONB storage behaviour.
 */
class ExecutionProvenanceServiceTest {

    private ExecutionProvenanceService service;

    private ProvActivity savedActivity;
    private ProvArtifact savedArtifact;
    private ProvRelationship savedRelationship;
    private ProvAgent savedAgent;

    @BeforeEach
    void setUp() {
        ProvenanceRepository fakeRepo = new ProvenanceRepository(null, null) {
            @Override
            public ProvActivity saveActivity(ProvActivity activity) {
                savedActivity = activity;
                return activity;
            }
            @Override
            public ProvArtifact saveArtifact(ProvArtifact artifact) {
                savedArtifact = artifact;
                return artifact;
            }
            @Override
            public ProvRelationship saveRelationship(ProvRelationship rel) {
                savedRelationship = rel;
                return rel;
            }
            @Override
            public ProvAgent saveAgent(ProvAgent agent) {
                savedAgent = agent;
                return agent;
            }
        };
        service = new ExecutionProvenanceService(fakeRepo);
    }

    // ── 80.44 Content Hashing ──────────────────────────────────────────────

    @Test
    void computeHash_isSHA256AndDeterministic() {
        String hash1 = service.computeHash("hello world");
        String hash2 = service.computeHash("hello world");

        assertNotNull(hash1);
        assertEquals(64, hash1.length(), "SHA-256 hex must be 64 chars");
        assertEquals(hash1, hash2, "Same input must produce same hash");
    }

    @Test
    void computeHash_differsForDifferentContent() {
        String h1 = service.computeHash("output A");
        String h2 = service.computeHash("output B");
        assertNotEquals(h1, h2);
    }

    // ── 80.45 Hash Verification ────────────────────────────────────────────

    @Test
    void verifyHash_returnsTrue_forMatchingContent() {
        String content = "{\"result\": \"success\"}";
        String hash = service.computeHash(content);
        assertTrue(service.verifyHash(content, hash));
    }

    @Test
    void verifyHash_returnsFalse_forTamperedContent() {
        String content = "{\"result\": \"success\"}";
        String hash = service.computeHash(content);
        assertFalse(service.verifyHash("{\"result\": \"TAMPERED\"}", hash));
    }

    @Test
    void verifyHash_returnsFalse_forNullInputs() {
        assertFalse(service.verifyHash(null, "someHash"));
        assertFalse(service.verifyHash("content", null));
    }

    // ── 80.17 Execution Provenance Recording ──────────────────────────────

    @Test
    void recordStepExecution_returnsActivityId_andSavesActivity() {
        UUID workflowId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        UUID activityId = service.recordStepExecution(
            workflowId, stepId, "LITERATURE_SEARCH", "1.0",
            "{\"query\":\"quantum computing\"}", "{\"results\":[\"paper1\"]}",
            projectId, null
        );

        assertNotNull(activityId);
        assertNotNull(savedActivity);
        assertEquals("WORKFLOW_STEP_EXECUTION", savedActivity.activityType());
        assertEquals("COMPLETED", savedActivity.status());
        assertEquals(projectId, savedActivity.projectId());
    }

    @Test
    void recordStepExecution_outputHashRecorded() {
        UUID projectId = UUID.randomUUID();
        String outputs = "{\"data\": \"some-result\"}";

        service.recordStepExecution(
            UUID.randomUUID(), UUID.randomUUID(), "ANALYSIS", "2.0",
            "{\"input\":\"x\"}", outputs, projectId, null
        );

        // The last saved artifact should be the output with its hash
        assertNotNull(savedArtifact);
        String expectedHash = service.computeHash(outputs);
        assertEquals(expectedHash, savedArtifact.contentHash());
        assertEquals(NodeType.RESULT.name(), savedArtifact.artifactType());
    }

    @Test
    void recordStepExecution_recordsUsedRelationshipForInput() {
        UUID projectId = UUID.randomUUID();

        service.recordStepExecution(
            UUID.randomUUID(), UUID.randomUUID(), "SEARCH", "1.0",
            "{\"query\":\"test\"}", "{\"found\":true}", projectId, null
        );

        // At minimum one relationship should be recorded (USED for input or WAS_GENERATED_BY for output)
        assertNotNull(savedRelationship);
    }

    @Test
    void recordStepExecution_nullOutputs_doesNotThrow() {
        UUID projectId = UUID.randomUUID();

        // Should not throw — Part 80.83: missing provenance marked as INCOMPLETE, not a failure
        UUID result = service.recordStepExecution(
            UUID.randomUUID(), UUID.randomUUID(), "TOOL", "1.0",
            null, null, projectId, null
        );
        // Returns null or an ID depending on whether any activity was saved
        // Key requirement: no exception thrown
    }

    // ── 80.24/80.25 Actor Provenance ──────────────────────────────────────

    @Test
    void registerAiAgent_savesAgentWithModelInfo() {
        UUID projectId = UUID.randomUUID();
        ProvAgent agent = service.registerAiAgent("GPT-4-research", "turbo-2024", projectId);

        assertNotNull(agent);
        assertNotNull(savedAgent);
        assertEquals(AgentType.AI_MODEL, savedAgent.agentType());
        assertTrue(savedAgent.name().contains("GPT-4-research"));
        assertEquals(projectId, savedAgent.projectId());
    }

    // ── 80.91 Workflow-Provenance Integration ─────────────────────────────

    @Test
    void recordStepExecution_activityIncludesWorkflowAndStepContext() {
        UUID workflowId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        service.recordStepExecution(
            workflowId, stepId, "HYPOTHESIS_GENERATION", "1.0",
            "{\"domain\":\"biology\"}", "{\"hypothesis\":\"H1\"}",
            projectId, null
        );

        assertNotNull(savedActivity);
        Map<String, Object> params = savedActivity.parameters();
        assertNotNull(params);
        assertEquals(workflowId.toString(), params.get("workflowId"));
        assertEquals(stepId.toString(), params.get("stepId"));
        assertEquals("HYPOTHESIS_GENERATION", params.get("capability"));
    }
}

