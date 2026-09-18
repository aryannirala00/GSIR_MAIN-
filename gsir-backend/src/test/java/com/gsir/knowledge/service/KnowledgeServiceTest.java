package com.gsir.knowledge.service;

import com.gsir.knowledge.adapter.DummyGraphAdapter;
import com.gsir.knowledge.model.*;
import com.gsir.projects.ProjectAccessService;
import com.gsir.projects.ProjectEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeServiceTest {

    private DefaultKnowledgeService service;
    private UUID projectId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        ProjectAccessService fakeAccessService = new ProjectAccessService(null) {
            @Override
            public ProjectEntity requireAccess(UUID projectId, UUID userId) {
                return new ProjectEntity(projectId, UUID.randomUUID(), "Test", "Test");
            }
        };
        service = new DefaultKnowledgeService(new DummyGraphAdapter(), fakeAccessService);
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void testEntityCreationAndProperties() {
        Map<String, Object> props = Map.of("atomicNumber", 6, "symbol", "C");
        KnowledgeEntity entity = service.createEntity("CONCEPT", "Carbon", Collections.emptyList(), props, "CHEMISTRY", projectId, userId);
        
        assertNotNull(entity.entityId());
        assertEquals("Carbon", entity.canonicalName());
        assertEquals("CONCEPT", entity.entityType());
        assertEquals("CHEMISTRY", entity.domain());
        assertEquals(6, entity.properties().get("atomicNumber"));
        assertEquals(ValidationStatus.UNVALIDATED, entity.validationStatus());
        assertEquals(1, entity.version());
        assertEquals(projectId, entity.projectId());
    }

    @Test
    void testRelationshipCreationWithConfidence() {
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        UUID provenanceId = UUID.randomUUID();
        
        KnowledgeRelationship rel = service.createRelationship(source, target, "BONDS_WITH", Map.of("bondType", "covalent"), 0.95, provenanceId, projectId, userId);
        
        assertEquals("BONDS_WITH", rel.relationshipType());
        assertEquals(0.95, rel.confidence());
        assertEquals(provenanceId, rel.provenanceId());
        assertEquals(source, rel.sourceEntityId());
        assertEquals(target, rel.targetEntityId());
        assertEquals("covalent", rel.properties().get("bondType"));
    }

    @Test
    void testScientificClaimWithProvenance() {
        UUID subject = UUID.randomUUID();
        UUID object = UUID.randomUUID();
        UUID provenanceId = UUID.randomUUID();
        
        ScientificClaim claim = service.createClaim("Carbon bonds with Oxygen to form CO2", ClaimType.SCIENTIFIC_CLAIM, subject, "FORMS", object, provenanceId, projectId, userId);
        
        assertEquals(ClaimType.SCIENTIFIC_CLAIM, claim.claimType());
        assertEquals(ClaimStatus.PROPOSED, claim.status());
        assertEquals(provenanceId, claim.provenanceId());
        assertEquals("FORMS", claim.predicate());
    }
}
