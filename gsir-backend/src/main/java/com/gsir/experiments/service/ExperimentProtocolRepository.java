package com.gsir.experiments.service;

import com.gsir.experiments.model.ExperimentProtocolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExperimentProtocolRepository extends JpaRepository<ExperimentProtocolEntity, UUID> {
    List<ExperimentProtocolEntity> findByExperimentId(UUID experimentId);
}

