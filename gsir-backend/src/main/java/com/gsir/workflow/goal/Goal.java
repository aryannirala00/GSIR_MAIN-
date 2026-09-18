package com.gsir.workflow.goal;

import java.util.UUID;

public record Goal(
        UUID id,
        String description,
        double priority,
        GoalStatus status) {
}

