package com.gsir.experiments.service;

import com.gsir.experiments.model.ExperimentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface ExperimentRepository extends JpaRepository<ExperimentEntity, UUID> {
    List<ExperimentEntity> findByProjectId(UUID projectId);
    Optional<ExperimentEntity> findByIdAndProjectId(UUID id, UUID projectId);
}

