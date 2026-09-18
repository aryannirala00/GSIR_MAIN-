package com.gsir.experiments.controller;

import com.gsir.experiments.model.ExperimentProtocolEntity;
import com.gsir.experiments.model.ExperimentRunEntity;
import com.gsir.experiments.model.ExperimentObservationEntity;
import com.gsir.experiments.model.ExperimentResultEntity;

import java.time.Instant;
import java.util.UUID;

public class ExperimentChildDtos {

    public record ProtocolDto(
            UUID id,
            UUID experimentId,
            int version,
            String preparation,
            String steps,
            String safetyRequirements,
            Instant createdAt
    ) {
        public static ProtocolDto fromEntity(ExperimentProtocolEntity entity) {
            return new ProtocolDto(
                    entity.getId(),
                    entity.getExperimentId(),
                    entity.getVersion(),
                    entity.getPreparation(),
                    entity.getSteps(),
                    entity.getSafetyRequirements(),
                    entity.getCreatedAt()
            );
        }
    }

    public record RunDto(
            UUID id,
            UUID experimentId,
            UUID protocolId,
            UUID operatorId,
            String status,
            Instant startTime,
            Instant endTime,
            Instant createdAt
    ) {
        public static RunDto fromEntity(ExperimentRunEntity entity) {
            return new RunDto(
                    entity.getId(),
                    entity.getExperimentId(),
                    entity.getProtocolId(),
                    entity.getOperatorId(),
                    entity.getStatus(),
                    entity.getStartTime(),
                    entity.getEndTime(),
                    entity.getCreatedAt()
            );
        }
    }

    public record ObservationDto(
            UUID id,
            UUID runId,
            UUID variableId,
            Instant timestamp,
            String value,
            String unit,
            String source,
            Double uncertainty,
            String uncertaintyType,
            String method,
            String assumptions,
            String covarianceData,
            Instant createdAt
    ) {
        public static ObservationDto fromEntity(ExperimentObservationEntity entity) {
            return new ObservationDto(
                    entity.getId(),
                    entity.getRunId(),
                    entity.getVariableId(),
                    entity.getTimestamp(),
                    entity.getValue(),
                    entity.getUnit(),
                    entity.getSource(),
                    entity.getUncertainty(),
                    entity.getUncertaintyType(),
                    entity.getMethod(),
                    entity.getAssumptions(),
                    entity.getCovarianceData(),
                    entity.getCreatedAt()
            );
        }
    }

    public record ResultDto(
            UUID id,
            UUID experimentId,
            UUID runId,
            String resultType,
            String value,
            String unit,
            Double confidence,
            Double uncertainty,
            String uncertaintyType,
            String method,
            String assumptions,
            String covarianceData,
            String validationStatus,
            Instant createdAt
    ) {
        public static ResultDto fromEntity(ExperimentResultEntity entity) {
            return new ResultDto(
                    entity.getId(),
                    entity.getExperimentId(),
                    entity.getRunId(),
                    entity.getResultType(),
                    entity.getValue(),
                    entity.getUnit(),
                    entity.getConfidence(),
                    entity.getUncertainty(),
                    entity.getUncertaintyType(),
                    entity.getMethod(),
                    entity.getAssumptions(),
                    entity.getCovarianceData(),
                    entity.getValidationStatus(),
                    entity.getCreatedAt()
            );
        }
    }
}
