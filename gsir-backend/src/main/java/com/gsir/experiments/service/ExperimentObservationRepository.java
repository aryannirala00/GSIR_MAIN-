package com.gsir.experiments.service;

import com.gsir.experiments.model.ExperimentObservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExperimentObservationRepository extends JpaRepository<ExperimentObservationEntity, UUID> {
    List<ExperimentObservationEntity> findByRunId(UUID runId);
}

