package com.gsir.ai.planning;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record Plan(
        UUID id,
        UUID goalId,
        List<PlanStep> steps,
        ExecutionConstraints constraints) {

    public Plan {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (goalId == null) throw new IllegalArgumentException("goalId cannot be null");
        if (steps == null) throw new IllegalArgumentException("steps cannot be null");
        steps = List.copyOf(steps);
        if (constraints == null) throw new IllegalArgumentException("constraints cannot be null");

        // Validate uniqueness of step IDs
        Set<UUID> stepIds = new HashSet<>();
        for (PlanStep step : steps) {
            if (!stepIds.add(step.id())) {
                throw new IllegalArgumentException("Duplicate step ID: " + step.id());
            }
        }

        // Validate dependencies exist
        for (PlanStep step : steps) {
            for (UUID dep : step.dependencies()) {
                if (!stepIds.contains(dep)) {
                    throw new IllegalArgumentException("Missing dependency: " + dep);
                }
            }
        }

        // Validate no cycles (Kahn's algorithm or DFS)
        Map<UUID, PlanStep> stepMap = steps.stream().collect(Collectors.toMap(PlanStep::id, s -> s));
        Set<UUID> visited = new HashSet<>();
        Set<UUID> recursionStack = new HashSet<>();

        for (PlanStep step : steps) {
            if (hasCycle(step.id(), stepMap, visited, recursionStack)) {
                throw new IllegalArgumentException("Cycle detected in plan dependencies");
            }
        }
    }

    private boolean hasCycle(UUID stepId, Map<UUID, PlanStep> stepMap, Set<UUID> visited, Set<UUID> recursionStack) {
        if (recursionStack.contains(stepId)) return true;
        if (visited.contains(stepId)) return false;

        visited.add(stepId);
        recursionStack.add(stepId);

        PlanStep step = stepMap.get(stepId);
        for (UUID dep : step.dependencies()) {
            if (hasCycle(dep, stepMap, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(stepId);
        return false;
    }
}
