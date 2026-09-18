package com.gsir.workflow.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.workflow.ResearchWorkflowEntity;
import com.gsir.workflow.ResearchWorkflowStepEntity;
import com.gsir.workflow.ResearchWorkflowStepRepository;
import com.gsir.workflow.model.WorkflowPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class WorkflowCompilerValidatorTest {

    private ResearchWorkflowStepRepository repository;
    private ObjectMapper objectMapper;
    private DefaultWorkflowValidator validator;
    private DefaultWorkflowCompiler compiler;
    private ResearchWorkflowEntity dummyWorkflow;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ResearchWorkflowStepRepository.class);
        objectMapper = new ObjectMapper();
        validator = new DefaultWorkflowValidator(repository, objectMapper);
        compiler = new DefaultWorkflowCompiler(repository, objectMapper);
        dummyWorkflow = new ResearchWorkflowEntity(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Goal");
    }

    @Test
    void testValidDAG() {
        UUID a = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID b = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID c = UUID.fromString("00000000-0000-0000-0000-000000000003");

        ResearchWorkflowStepEntity stepA = new ResearchWorkflowStepEntity(a, dummyWorkflow.getId(), "A", "cap", "{}", "[]");
        ResearchWorkflowStepEntity stepB = new ResearchWorkflowStepEntity(b, dummyWorkflow.getId(), "B", "cap", "{}", "[\"" + a + "\"]");
        ResearchWorkflowStepEntity stepC = new ResearchWorkflowStepEntity(c, dummyWorkflow.getId(), "C", "cap", "{}", "[\"" + b + "\"]");

        when(repository.findByWorkflowId(any())).thenReturn(List.of(stepA, stepB, stepC));

        assertTrue(validator.validate(dummyWorkflow));

        WorkflowPlan plan = compiler.compile(dummyWorkflow);
        assertEquals(3, plan.executionOrder().size());
        assertEquals(a.toString(), plan.executionOrder().get(0)); // deterministic start
        assertEquals(1, plan.runnableNodes().size()); // only A is runnable at start
    }

    @Test
    void testCycleDetection() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        ResearchWorkflowStepEntity stepA = new ResearchWorkflowStepEntity(a, dummyWorkflow.getId(), "A", "cap", "{}", "[\"" + b + "\"]");
        ResearchWorkflowStepEntity stepB = new ResearchWorkflowStepEntity(b, dummyWorkflow.getId(), "B", "cap", "{}", "[\"" + a + "\"]");

        when(repository.findByWorkflowId(any())).thenReturn(List.of(stepA, stepB));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.validate(dummyWorkflow));
        assertTrue(ex.getMessage().contains("Cycle detected"));
    }

    @Test
    void testSelfDependency() {
        UUID a = UUID.randomUUID();
        ResearchWorkflowStepEntity stepA = new ResearchWorkflowStepEntity(a, dummyWorkflow.getId(), "A", "cap", "{}", "[\"" + a + "\"]");

        when(repository.findByWorkflowId(any())).thenReturn(List.of(stepA));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.validate(dummyWorkflow));
        assertTrue(ex.getMessage().contains("self-references"));
    }

    @Test
    void testMissingDependency() {
        UUID a = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        ResearchWorkflowStepEntity stepA = new ResearchWorkflowStepEntity(a, dummyWorkflow.getId(), "A", "cap", "{}", "[\"" + missing + "\"]");

        when(repository.findByWorkflowId(any())).thenReturn(List.of(stepA));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.validate(dummyWorkflow));
        assertTrue(ex.getMessage().contains("Missing dependency"));
    }

    @Test
    void testDuplicateNode() {
        UUID a = UUID.randomUUID();
        ResearchWorkflowStepEntity stepA = new ResearchWorkflowStepEntity(a, dummyWorkflow.getId(), "A", "cap", "{}", "[]");
        ResearchWorkflowStepEntity stepA_dup = new ResearchWorkflowStepEntity(a, dummyWorkflow.getId(), "A_dup", "cap", "{}", "[]");

        when(repository.findByWorkflowId(any())).thenReturn(List.of(stepA, stepA_dup));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.validate(dummyWorkflow));
        assertTrue(ex.getMessage().contains("Duplicate node ID detected"));
    }

    @Test
    void testMalformedConfiguration() {
        UUID a = UUID.randomUUID();
        // Invalid JSON for inputs
        ResearchWorkflowStepEntity stepA = new ResearchWorkflowStepEntity(a, dummyWorkflow.getId(), "A", "cap", "not-a-json", "[]");

        when(repository.findByWorkflowId(any())).thenReturn(List.of(stepA));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.validate(dummyWorkflow));
        assertTrue(ex.getMessage().contains("Malformed configuration inputs"));
    }
    
    @Test
    void testDeterministicExecutionOrder() {
        UUID a = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID b = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // Both A and B have 0 dependencies. They should be ordered deterministically by UUID.
        ResearchWorkflowStepEntity stepA = new ResearchWorkflowStepEntity(a, dummyWorkflow.getId(), "A", "cap", "{}", "[]");
        ResearchWorkflowStepEntity stepB = new ResearchWorkflowStepEntity(b, dummyWorkflow.getId(), "B", "cap", "{}", "[]");

        when(repository.findByWorkflowId(any())).thenReturn(List.of(stepA, stepB)); // Pass in unsorted/random order

        WorkflowPlan plan = compiler.compile(dummyWorkflow);
        // "b" is lexicographically smaller than "a"
        assertEquals(b.toString(), plan.executionOrder().get(0));
        assertEquals(a.toString(), plan.executionOrder().get(1));
        assertEquals(b.toString(), plan.runnableNodes().get(0).nodeId());
        assertEquals(a.toString(), plan.runnableNodes().get(1).nodeId());
    }
}
