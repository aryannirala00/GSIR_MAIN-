package com.gsir.experiments.service;

import com.gsir.common.error.GsirException;
import com.gsir.experiments.model.ExperimentEntity;
import com.gsir.experiments.model.ExperimentStatus;
import com.gsir.projects.ProjectAccessService;
import com.gsir.projects.ProjectEntity;
import com.gsir.provenance.repository.ProvenanceRepository;
import com.gsir.provenance.model.ProvActivity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.util.Map;

@Service
public class DefaultExperimentService implements ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final ExperimentProtocolRepository protocolRepository;
    private final ExperimentRunRepository runRepository;
    private final ExperimentObservationRepository observationRepository;
    private final ExperimentResultRepository resultRepository;
    private final ProjectAccessService projectAccessService;
    private final ProvenanceRepository provenanceRepository;

    public DefaultExperimentService(ExperimentRepository experimentRepository,
                                    ExperimentProtocolRepository protocolRepository,
                                    ExperimentRunRepository runRepository,
                                    ExperimentObservationRepository observationRepository,
                                    ExperimentResultRepository resultRepository,
                                    ProjectAccessService projectAccessService,
                                    ProvenanceRepository provenanceRepository) {
        this.experimentRepository = experimentRepository;
        this.protocolRepository = protocolRepository;
        this.runRepository = runRepository;
        this.observationRepository = observationRepository;
        this.resultRepository = resultRepository;
        this.projectAccessService = projectAccessService;
        this.provenanceRepository = provenanceRepository;
    }

    @Override
    @Transactional
    public ExperimentEntity createExperiment(UUID projectId, UUID userId, String name, String description, String researchQuestion, UUID hypothesisId) {
        ProjectEntity project = projectAccessService.requireAccess(projectId, userId);

        ExperimentEntity experiment = new ExperimentEntity();
        experiment.setProjectId(projectId);
        experiment.setOwnerId(userId);
        experiment.setName(name);
        experiment.setDescription(description);
        experiment.setResearchQuestion(researchQuestion);
        experiment.setHypothesisId(hypothesisId);
        experiment.setStatus(ExperimentStatus.DRAFT);
        experiment.setVersion(1);

        experiment = experimentRepository.save(experiment);

        // Record Provenance
        ProvActivity activity = new ProvActivity(
            UUID.randomUUID(),
            projectId,
            "EXPERIMENT_CREATED",
            "COMPLETED",
            Instant.now(),
            Instant.now(),
            Map.of("experimentId", experiment.getId(), "name", name),
            Map.of("userId", userId),
            Instant.now()
        );
        provenanceRepository.saveActivity(activity);

        return experiment;
    }

    @Override
    @Transactional(readOnly = true)
    public ExperimentEntity getExperiment(UUID experimentId, UUID projectId, UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        return experimentRepository.findByIdAndProjectId(experimentId, projectId)
                .orElseThrow(() -> new GsirException("EXPERIMENT_NOT_FOUND", "Experiment not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExperimentEntity> listExperiments(UUID projectId, UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        return experimentRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional
    public ExperimentEntity updateExperimentStatus(UUID experimentId, UUID projectId, UUID userId, String status) {
        projectAccessService.requireAccess(projectId, userId);
        ExperimentEntity experiment = experimentRepository.findByIdAndProjectId(experimentId, projectId)
                .orElseThrow(() -> new GsirException("EXPERIMENT_NOT_FOUND", "Experiment not found", HttpStatus.NOT_FOUND));

        ExperimentStatus newStatus;
        try {
            newStatus = ExperimentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GsirException("INVALID_STATUS", "Invalid experiment status", HttpStatus.BAD_REQUEST);
        }

        validateTransition(experiment.getStatus(), newStatus);

        experiment.setStatus(newStatus);
        experiment = experimentRepository.save(experiment);

        // Record Provenance
        ProvActivity activity = new ProvActivity(
            UUID.randomUUID(),
            projectId,
            "EXPERIMENT_STATUS_UPDATED",
            "COMPLETED",
            Instant.now(),
            Instant.now(),
            Map.of("experimentId", experiment.getId(), "newStatus", newStatus.name()),
            Map.of("userId", userId),
            Instant.now()
        );
        provenanceRepository.saveActivity(activity);

        return experiment;
    }

    private void validateTransition(ExperimentStatus current, ExperimentStatus next) {
        if (current == next) return;
        boolean valid = switch (current) {
            case DRAFT -> next == ExperimentStatus.DESIGNED || next == ExperimentStatus.CANCELLED;
            case DESIGNED -> next == ExperimentStatus.READY || next == ExperimentStatus.DRAFT || next == ExperimentStatus.CANCELLED;
            case READY -> next == ExperimentStatus.SCHEDULED || next == ExperimentStatus.CANCELLED;
            case SCHEDULED -> next == ExperimentStatus.RUNNING || next == ExperimentStatus.CANCELLED;
            case RUNNING -> next == ExperimentStatus.PAUSED || next == ExperimentStatus.COMPLETED || next == ExperimentStatus.FAILED || next == ExperimentStatus.CANCELLED;
            case PAUSED -> next == ExperimentStatus.RUNNING || next == ExperimentStatus.CANCELLED;
            case COMPLETED, FAILED, CANCELLED -> next == ExperimentStatus.ARCHIVED;
            case ARCHIVED -> false;
        };
        if (!valid) {
            throw new GsirException("INVALID_TRANSITION", "Cannot transition experiment from " + current + " to " + next, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public com.gsir.experiments.model.ExperimentProtocolEntity createProtocol(UUID projectId, UUID experimentId, UUID userId, String preparation, String steps, String safetyRequirements) {
        getExperiment(experimentId, projectId, userId); // validates access and ownership

        com.gsir.experiments.model.ExperimentProtocolEntity protocol = new com.gsir.experiments.model.ExperimentProtocolEntity();
        protocol.setExperimentId(experimentId);
        protocol.setPreparation(preparation);
        protocol.setSteps(steps);
        protocol.setSafetyRequirements(safetyRequirements);
        protocol.setVersion(1);

        protocol = protocolRepository.save(protocol);

        provenanceRepository.saveActivity(new ProvActivity(
                UUID.randomUUID(), projectId, "PROTOCOL_CREATED", "COMPLETED",
                Instant.now(), Instant.now(),
                Map.of("experimentId", experimentId, "protocolId", protocol.getId()),
                Map.of("userId", userId), Instant.now()
        ));
        return protocol;
    }

    @Override
    @Transactional
    public com.gsir.experiments.model.ExperimentRunEntity createRun(UUID projectId, UUID experimentId, UUID userId, UUID protocolId) {
        getExperiment(experimentId, projectId, userId); // Access check
        
        if (protocolId != null) {
            protocolRepository.findById(protocolId).filter(p -> p.getExperimentId().equals(experimentId))
                .orElseThrow(() -> new GsirException("INVALID_PROTOCOL", "Protocol not found or belongs to another experiment", HttpStatus.BAD_REQUEST));
        }

        com.gsir.experiments.model.ExperimentRunEntity run = new com.gsir.experiments.model.ExperimentRunEntity();
        run.setExperimentId(experimentId);
        run.setProtocolId(protocolId);
        run.setOperatorId(userId);
        run.setStatus("CREATED");
        run.setStartTime(Instant.now());

        run = runRepository.save(run);

        provenanceRepository.saveActivity(new ProvActivity(
                UUID.randomUUID(), projectId, "RUN_CREATED", "COMPLETED",
                Instant.now(), Instant.now(),
                Map.of("experimentId", experimentId, "runId", run.getId()),
                Map.of("userId", userId), Instant.now()
        ));
        return run;
    }

    @Override
    @Transactional
    public com.gsir.experiments.model.ExperimentObservationEntity recordObservation(UUID projectId, UUID experimentId, UUID runId, UUID userId, UUID variableId, String value, String unit, String source, Double uncertainty, String uncertaintyType, String method, String assumptions, String covarianceData) {
        getExperiment(experimentId, projectId, userId);
        runRepository.findById(runId).filter(r -> r.getExperimentId().equals(experimentId))
            .orElseThrow(() -> new GsirException("INVALID_RUN", "Run not found or belongs to another experiment", HttpStatus.BAD_REQUEST));

        com.gsir.experiments.model.ExperimentObservationEntity obs = new com.gsir.experiments.model.ExperimentObservationEntity();
        obs.setRunId(runId);
        obs.setVariableId(variableId);
        obs.setTimestamp(Instant.now());
        obs.setValue(value);
        obs.setUnit(unit);
        obs.setSource(source);
        obs.setUncertainty(uncertainty);
        obs.setUncertaintyType(uncertaintyType);
        obs.setMethod(method);
        obs.setAssumptions(assumptions);
        obs.setCovarianceData(covarianceData);

        obs = observationRepository.save(obs);

        provenanceRepository.saveActivity(new ProvActivity(
                UUID.randomUUID(), projectId, "OBSERVATION_RECORDED", "COMPLETED",
                Instant.now(), Instant.now(),
                Map.of("runId", runId, "observationId", obs.getId()),
                Map.of("userId", userId), Instant.now()
        ));
        return obs;
    }

    @Override
    @Transactional
    public com.gsir.experiments.model.ExperimentResultEntity recordResult(UUID projectId, UUID experimentId, UUID runId, UUID userId, String resultType, String value, String unit, Double confidence, Double uncertainty, String uncertaintyType, String method, String assumptions, String covarianceData) {
        getExperiment(experimentId, projectId, userId);
        if (runId != null) {
            runRepository.findById(runId).filter(r -> r.getExperimentId().equals(experimentId))
                .orElseThrow(() -> new GsirException("INVALID_RUN", "Run not found or belongs to another experiment", HttpStatus.BAD_REQUEST));
        }

        com.gsir.experiments.model.ExperimentResultEntity result = new com.gsir.experiments.model.ExperimentResultEntity();
        result.setExperimentId(experimentId);
        result.setRunId(runId);
        result.setResultType(resultType);
        result.setValue(value);
        result.setUnit(unit);
        result.setConfidence(confidence);
        result.setUncertainty(uncertainty);
        result.setUncertaintyType(uncertaintyType);
        result.setMethod(method);
        result.setAssumptions(assumptions);
        result.setCovarianceData(covarianceData);
        result.setValidationStatus("STORED");

        result = resultRepository.save(result);

        provenanceRepository.saveActivity(new ProvActivity(
                UUID.randomUUID(), projectId, "RESULT_STORED", "COMPLETED",
                Instant.now(), Instant.now(),
                Map.of("experimentId", experimentId, "resultId", result.getId()),
                Map.of("userId", userId), Instant.now()
        ));
        return result;
    }
}
