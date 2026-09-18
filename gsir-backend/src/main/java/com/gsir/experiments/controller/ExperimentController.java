package com.gsir.experiments.controller;

import com.gsir.experiments.model.ExperimentEntity;
import com.gsir.experiments.service.ExperimentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/experiments")
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @PostMapping
    public ResponseEntity<ExperimentDto> createExperiment(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody CreateExperimentRequest request) {
        ExperimentEntity experiment = experimentService.createExperiment(
                projectId,
                userId,
                request.name(),
                request.description(),
                request.researchQuestion(),
                request.hypothesisId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ExperimentDto.fromEntity(experiment));
    }

    @GetMapping
    public ResponseEntity<List<ExperimentDto>> listExperiments(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId) {
        List<ExperimentDto> experiments = experimentService.listExperiments(projectId, userId).stream()
                .map(ExperimentDto::fromEntity).toList();
        return ResponseEntity.ok(experiments);
    }

    @GetMapping("/{experimentId}")
    public ResponseEntity<ExperimentDto> getExperiment(
            @PathVariable UUID projectId,
            @PathVariable UUID experimentId,
            @AuthenticationPrincipal UUID userId) {
        ExperimentEntity experiment = experimentService.getExperiment(experimentId, projectId, userId);
        return ResponseEntity.ok(ExperimentDto.fromEntity(experiment));
    }

    @PatchMapping("/{experimentId}/status")
    public ResponseEntity<ExperimentDto> updateStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID experimentId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody UpdateStatusRequest request) {
        ExperimentEntity experiment = experimentService.updateExperimentStatus(experimentId, projectId, userId, request.status());
        return ResponseEntity.ok(ExperimentDto.fromEntity(experiment));
    }

    @PostMapping("/{experimentId}/protocols")
    public ResponseEntity<ExperimentChildDtos.ProtocolDto> createProtocol(
            @PathVariable UUID projectId,
            @PathVariable UUID experimentId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody CreateProtocolRequest request) {
        var protocol = experimentService.createProtocol(projectId, experimentId, userId, request.preparation(), request.steps(), request.safetyRequirements());
        return ResponseEntity.status(HttpStatus.CREATED).body(ExperimentChildDtos.ProtocolDto.fromEntity(protocol));
    }

    @PostMapping("/{experimentId}/runs")
    public ResponseEntity<ExperimentChildDtos.RunDto> createRun(
            @PathVariable UUID projectId,
            @PathVariable UUID experimentId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody CreateRunRequest request) {
        var run = experimentService.createRun(projectId, experimentId, userId, request.protocolId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ExperimentChildDtos.RunDto.fromEntity(run));
    }

    @PostMapping("/{experimentId}/runs/{runId}/observations")
    public ResponseEntity<ExperimentChildDtos.ObservationDto> recordObservation(
            @PathVariable UUID projectId,
            @PathVariable UUID experimentId,
            @PathVariable UUID runId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody RecordObservationRequest request) {
        var obs = experimentService.recordObservation(projectId, experimentId, runId, userId, request.variableId(), request.value(), request.unit(), request.source(), request.uncertainty(), request.uncertaintyType(), request.method(), request.assumptions(), request.covarianceData());
        return ResponseEntity.status(HttpStatus.CREATED).body(ExperimentChildDtos.ObservationDto.fromEntity(obs));
    }

    @PostMapping("/{experimentId}/results")
    public ResponseEntity<ExperimentChildDtos.ResultDto> recordResult(
            @PathVariable UUID projectId,
            @PathVariable UUID experimentId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody RecordResultRequest request) {
        var result = experimentService.recordResult(projectId, experimentId, request.runId(), userId, request.resultType(), request.value(), request.unit(), request.confidence(), request.uncertainty(), request.uncertaintyType(), request.method(), request.assumptions(), request.covarianceData());
        return ResponseEntity.status(HttpStatus.CREATED).body(ExperimentChildDtos.ResultDto.fromEntity(result));
    }

    public record CreateExperimentRequest(String name, String description, String researchQuestion, UUID hypothesisId) {}
    public record UpdateStatusRequest(String status) {}
    public record CreateProtocolRequest(String preparation, String steps, String safetyRequirements) {}
    public record CreateRunRequest(UUID protocolId) {}
    public record RecordObservationRequest(UUID variableId, String value, String unit, String source, Double uncertainty, String uncertaintyType, String method, String assumptions, String covarianceData) {}
    public record RecordResultRequest(UUID runId, String resultType, String value, String unit, Double confidence, Double uncertainty, String uncertaintyType, String method, String assumptions, String covarianceData) {}
}

