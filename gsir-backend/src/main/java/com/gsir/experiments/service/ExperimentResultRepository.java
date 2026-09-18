package com.gsir.experiments.service;

import com.gsir.experiments.model.ExperimentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExperimentResultRepository extends JpaRepository<ExperimentResultEntity, UUID> {
    List<ExperimentResultEntity> findByExperimentId(UUID experimentId);
    List<ExperimentResultEntity> findByRunId(UUID runId);
}

