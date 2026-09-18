package com.gsir.assurance.service;

import com.gsir.knowledge.adapter.GraphStorageAdapter;
import com.gsir.knowledge.model.KnowledgeRelationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProvenanceQueryEngineTest {

    @Mock
    private GraphStorageAdapter graphAdapter;

    private ProvenanceQueryEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ProvenanceQueryEngine(graphAdapter);
    }

    @Test
    void getBackwardProvenance_findsPaths() {
        UUID projectId = UUID.randomUUID();
        UUID a = UUID.randomUUID(); // Source
        UUID b = UUID.randomUUID(); // Intermediate
        UUID c = UUID.randomUUID(); // Target

        KnowledgeRelationship edge1 = new KnowledgeRelationship(UUID.randomUUID(), "GENERATED", a, b, Map.of(), 1.0, null, List.of(), null, null, "ACTIVE", projectId, 1, false, null);
        KnowledgeRelationship edge2 = new KnowledgeRelationship(UUID.randomUUID(), "INFLUENCED", b, c, Map.of(), 1.0, null, List.of(), null, null, "ACTIVE", projectId, 1, false, null);

        when(graphAdapter.retrieveRelationshipsByTarget(c, projectId)).thenReturn(List.of(edge2));
        when(graphAdapter.retrieveRelationshipsByTarget(b, projectId)).thenReturn(List.of(edge1));
        when(graphAdapter.retrieveRelationshipsByTarget(a, projectId)).thenReturn(Collections.emptyList());

        List<KnowledgeRelationship> results = engine.getBackwardProvenance(c, projectId, 5);

        assertEquals(2, results.size());
        assertEquals(edge2, results.get(0));
        assertEquals(edge1, results.get(1));
    }
}
