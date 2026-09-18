package com.gsir.provenance.service;

import com.gsir.provenance.model.*;
import com.gsir.provenance.repository.ProvenanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProvenanceServiceTest {

    private ProvenanceService service;
    private UUID projectId;

    private List<ProvRelationship> fakeRelationships = List.of();
    private boolean fakeArtifactExists = false;
    private boolean fakeActivityExists = false;
    private boolean fakeAgentExists = false;

    private Map<UUID, List<ProvRelationship>> forwardGraph = new java.util.HashMap<>();
    private Map<UUID, List<ProvRelationship>> backwardGraph = new java.util.HashMap<>();

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        forwardGraph.clear();
        backwardGraph.clear();
        
        ProvenanceRepository fakeRepository = new ProvenanceRepository(null, null) {
            @Override
            public ProvAgent saveAgent(ProvAgent agent) { return agent; }
            @Override
            public List<ProvRelationship> findForwardLineage(UUID sourceId, UUID projectId) {
                return forwardGraph.getOrDefault(sourceId, List.of());
            }
            @Override
            public List<ProvRelationship> findBackwardLineage(UUID targetId, UUID projectId) {
                return backwardGraph.getOrDefault(targetId, List.of());
            }
            @Override
            public List<ProvArtifact> findArtifactsByProjectId(UUID projectId) { return List.of(); }
            @Override
            public List<ProvActivity> findActivitiesByProjectId(UUID projectId) { return List.of(); }
            @Override
            public List<ProvAgent> findAgentsByProjectId(UUID projectId) { return List.of(); }
            @Override
            public List<ProvRelationship> findRelationshipsByProjectId(UUID projectId) { return fakeRelationships; }
            @Override
            public boolean artifactExists(UUID id, UUID pId) { return fakeArtifactExists; }
            @Override
            public boolean activityExists(UUID id, UUID pId) { return fakeActivityExists; }
            @Override
            public boolean agentExists(UUID id, UUID pId) { return fakeAgentExists; }
        };
        service = new ProvenanceService(fakeRepository);
    }

    @Test
    void testRegisterAgent() {
        ProvAgent agent = service.registerAgent(AgentType.AI_MODEL, "GPT-4", Collections.emptyMap(), projectId);
        assertNotNull(agent.id());
        assertEquals(AgentType.AI_MODEL, agent.agentType());
        assertEquals("GPT-4", agent.name());
        assertEquals(projectId, agent.projectId());
    }

    @Test
    void testExportProvenance() {
        Map<String, Object> export = service.exportProvenance(projectId);
        assertEquals(projectId, export.get("projectId"));
        assertTrue(((List<?>) export.get("relationships")).isEmpty());
    }

    @Test
    void testValidateProvenance_ValidGraph() {
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        fakeRelationships = List.of(new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_GENERATED_BY, source, target, Instant.now()));
        fakeArtifactExists = true;
        
        assertTrue(service.validateProvenance(projectId));
    }

    @Test
    void testValidateProvenance_BrokenRelationship_MissingNode() {
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        fakeRelationships = List.of(new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_GENERATED_BY, source, target, Instant.now()));
        fakeArtifactExists = false;
        fakeActivityExists = false;
        fakeAgentExists = false;
        
        assertFalse(service.validateProvenance(projectId));
    }

    @Test
    void testValidateProvenance_EmptyGraph() {
        fakeRelationships = List.of();
        assertTrue(service.validateProvenance(projectId), "Empty graph should be valid by default");
    }

    @Test
    void testGetImpactAnalysis_BranchingAndCycles() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        
        ProvRelationship r1 = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_DERIVED_FROM, a, b, Instant.now());
        ProvRelationship r2 = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_DERIVED_FROM, a, c, Instant.now());
        ProvRelationship r3 = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_DERIVED_FROM, b, a, Instant.now()); // Cycle
        
        forwardGraph.put(a, List.of(r1, r2));
        forwardGraph.put(b, List.of(r3));
        
        List<ProvRelationship> impact = service.getImpactAnalysis(a, projectId, 10);
        assertEquals(3, impact.size());
        assertTrue(impact.contains(r1));
        assertTrue(impact.contains(r2));
        assertTrue(impact.contains(r3));
    }
    
    @Test
    void testCompareProvenance() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID sourceCommon = UUID.randomUUID();
        
        ProvRelationship commonEdge = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_GENERATED_BY, sourceCommon, a, Instant.now());
        ProvRelationship sourceOnlyEdge = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_GENERATED_BY, UUID.randomUUID(), a, Instant.now());
        ProvRelationship targetOnlyEdge = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_GENERATED_BY, UUID.randomUUID(), b, Instant.now());
        
        backwardGraph.put(a, List.of(commonEdge, sourceOnlyEdge));
        backwardGraph.put(b, List.of(commonEdge, targetOnlyEdge));
        
        Map<String, Object> result = service.compareProvenance(projectId, a, b);
        
        List<?> common = (List<?>) result.get("commonLineage");
        List<?> sourceOnly = (List<?>) result.get("sourceOnlyLineage");
        List<?> targetOnly = (List<?>) result.get("targetOnlyLineage");
        
        assertEquals(1, common.size());
        assertEquals(1, sourceOnly.size());
        assertEquals(1, targetOnly.size());
        assertFalse((Boolean) result.get("isIdentical"));
    }

    @Test
    void testGetEvidenceChain() {
        UUID claimId = UUID.randomUUID();
        UUID evidence1 = UUID.randomUUID();
        UUID evidence2 = UUID.randomUUID();
        UUID unrelated = UUID.randomUUID();

        ProvRelationship r1 = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.SUPPORTED_BY, evidence1, claimId, Instant.now());
        ProvRelationship r2 = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.CONTRADICTED_BY, evidence2, claimId, Instant.now());
        ProvRelationship r3 = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_GENERATED_BY, unrelated, claimId, Instant.now());

        backwardGraph.put(claimId, List.of(r1, r2, r3));

        List<ProvRelationship> chain = service.getEvidenceChain(claimId, projectId, 10);
        assertEquals(2, chain.size());
        assertTrue(chain.contains(r1));
        assertTrue(chain.contains(r2));
        assertFalse(chain.contains(r3));
    }

    @Test
    void testGetClaimChain() {
        UUID evidenceId = UUID.randomUUID();
        UUID claim1 = UUID.randomUUID();
        UUID unrelated = UUID.randomUUID();

        ProvRelationship r1 = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.SUPPORTED_BY, evidenceId, claim1, Instant.now());
        ProvRelationship r2 = new ProvRelationship(UUID.randomUUID(), projectId, RelationshipType.WAS_GENERATED_BY, evidenceId, unrelated, Instant.now());

        forwardGraph.put(evidenceId, List.of(r1, r2));

        List<ProvRelationship> chain = service.getClaimChain(evidenceId, projectId, 10);
        assertEquals(1, chain.size());
        assertTrue(chain.contains(r1));
        assertFalse(chain.contains(r2));
    }
}
