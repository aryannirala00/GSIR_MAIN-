package com.gsir.ai.planning;

import com.gsir.workflow.goal.Goal;
import com.gsir.workflow.goal.GoalStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class Part89PlanningEngineTest {

    @Test
    void testValidPlanAndGoalIntegration() {
        Goal goal = new Goal(UUID.randomUUID(), "Complete task", 1.0, GoalStatus.ACTIVE);
        
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();

        PlanStep step1 = new PlanStep(step1Id, "Step 1", List.of(), 10.0);
        PlanStep step2 = new PlanStep(step2Id, "Step 2", List.of(step1Id), 15.0);

        ExecutionConstraints constraints = new ExecutionConstraints(50, 3600000L, 100.0, 200, 10, 2048L, 50);

        Plan plan = new Plan(UUID.randomUUID(), goal.id(), List.of(step1, step2), constraints);

        assertNotNull(plan.id());
        assertEquals(goal.id(), plan.goalId());
        assertEquals(2, plan.steps().size());
        
        PlanStep retrievedStep = plan.steps().get(1);
        assertEquals(step2Id, retrievedStep.id());
        assertEquals(1, retrievedStep.dependencies().size());
        assertEquals(step1Id, retrievedStep.dependencies().get(0));
        assertEquals(15.0, retrievedStep.estimatedCost());
    }

    @Test
    void testMissingDependency() {
        UUID step1Id = UUID.randomUUID();
        UUID invalidDepId = UUID.randomUUID();
        
        PlanStep step1 = new PlanStep(step1Id, "Step 1", List.of(invalidDepId), 10.0);
        ExecutionConstraints constraints = new ExecutionConstraints(50, 3600000L, 100.0, 200, 10, 2048L, 50);

        assertThrows(IllegalArgumentException.class, () -> {
            new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(step1), constraints);
        }, "Should throw on missing dependency");
    }

    @Test
    void testCyclicDependency() {
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();

        // Cyclic: 1 -> 2 -> 1
        PlanStep step1 = new PlanStep(step1Id, "Step 1", List.of(step2Id), 10.0);
        PlanStep step2 = new PlanStep(step2Id, "Step 2", List.of(step1Id), 15.0);

        ExecutionConstraints constraints = new ExecutionConstraints(50, 3600000L, 100.0, 200, 10, 2048L, 50);

        assertThrows(IllegalArgumentException.class, () -> {
            new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(step1, step2), constraints);
        }, "Should throw on cyclic dependency");
    }

    @Test
    void testDuplicateStepId() {
        UUID step1Id = UUID.randomUUID();

        PlanStep step1 = new PlanStep(step1Id, "Step 1", List.of(), 10.0);
        PlanStep step2 = new PlanStep(step1Id, "Step 2", List.of(), 15.0); // Duplicate ID

        ExecutionConstraints constraints = new ExecutionConstraints(50, 3600000L, 100.0, 200, 10, 2048L, 50);

        assertThrows(IllegalArgumentException.class, () -> {
            new Plan(UUID.randomUUID(), UUID.randomUUID(), List.of(step1, step2), constraints);
        }, "Should throw on duplicate step ID");
    }

    @Test
    void testImmutableDependencies() {
        UUID step1Id = UUID.randomUUID();
        List<UUID> mutableList = new java.util.ArrayList<>();
        
        PlanStep step = new PlanStep(step1Id, "Step 1", mutableList, 10.0);
        mutableList.add(UUID.randomUUID()); // Should not affect the step

        assertEquals(0, step.dependencies().size());
        
        assertThrows(UnsupportedOperationException.class, () -> {
            step.dependencies().add(UUID.randomUUID());
        });
    }
}
