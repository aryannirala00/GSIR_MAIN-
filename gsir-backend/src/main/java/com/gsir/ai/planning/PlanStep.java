package com.gsir.ai.planning;

import java.util.List;
import java.util.UUID;

public record PlanStep(
        UUID id,
        String description,
        List<UUID> dependencies,
        double estimatedCost) {

    public PlanStep {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("description cannot be empty");
        if (dependencies == null) throw new IllegalArgumentException("dependencies cannot be null");
        dependencies = List.copyOf(dependencies); // Ensure immutability
    }
}
