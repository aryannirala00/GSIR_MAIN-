package com.gsir.experiments.service;

import com.gsir.experiments.model.ExperimentRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExperimentRunRepository extends JpaRepository<ExperimentRunEntity, UUID> {
    List<ExperimentRunEntity> findByExperimentId(UUID experimentId);
}

