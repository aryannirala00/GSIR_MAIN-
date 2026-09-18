package com.gsir.experiments.service;

import com.gsir.experiments.model.ExperimentEntity;
import java.util.List;
import java.util.UUID;

public interface ExperimentService {
    ExperimentEntity createExperiment(UUID projectId, UUID userId, String name, String description, String researchQuestion, UUID hypothesisId);
    ExperimentEntity getExperiment(UUID experimentId, UUID projectId, UUID userId);
    List<ExperimentEntity> listExperiments(UUID projectId, UUID userId);
    ExperimentEntity updateExperimentStatus(UUID experimentId, UUID projectId, UUID userId, String status);

    com.gsir.experiments.model.ExperimentProtocolEntity createProtocol(UUID projectId, UUID experimentId, UUID userId, String preparation, String steps, String safetyRequirements);
    
    com.gsir.experiments.model.ExperimentRunEntity createRun(UUID projectId, UUID experimentId, UUID userId, UUID protocolId);
    
    com.gsir.experiments.model.ExperimentObservationEntity recordObservation(UUID projectId, UUID experimentId, UUID runId, UUID userId, UUID variableId, String value, String unit, String source, Double uncertainty, String uncertaintyType, String method, String assumptions, String covarianceData);
    
    com.gsir.experiments.model.ExperimentResultEntity recordResult(UUID projectId, UUID experimentId, UUID runId, UUID userId, String resultType, String value, String unit, Double confidence, Double uncertainty, String uncertaintyType, String method, String assumptions, String covarianceData);
}

