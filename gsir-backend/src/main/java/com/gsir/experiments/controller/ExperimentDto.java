package com.gsir.experiments.controller;

import com.gsir.experiments.model.ExperimentEntity;
import com.gsir.experiments.model.ExperimentStatus;

import java.time.Instant;
import java.util.UUID;

public record ExperimentDto(
    UUID id,
    UUID projectId,
    String name,
    String description,
    String objective,
    String researchQuestion,
    UUID hypothesisId,
    ExperimentStatus status,
    UUID ownerId,
    int version,
    Instant createdAt,
    Instant updatedAt
) {
    public static ExperimentDto fromEntity(ExperimentEntity entity) {
        return new ExperimentDto(
            entity.getId(),
            entity.getProjectId(),
            entity.getName(),
            entity.getDescription(),
            entity.getObjective(),
            entity.getResearchQuestion(),
            entity.getHypothesisId(),
            entity.getStatus(),
            entity.getOwnerId(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

