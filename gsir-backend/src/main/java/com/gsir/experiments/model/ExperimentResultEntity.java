package com.gsir.experiments.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "experiment_results")
public class ExperimentResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "experiment_id", nullable = false)
    private UUID experimentId;

    @Column(name = "run_id")
    private UUID runId;

    @Column(name = "result_type", nullable = false)
    private String resultType;

    @Column(name = "\"value\"", nullable = false)
    private String value;

    private String unit;
    private Double confidence;

    @Column(name = "uncertainty")
    private Double uncertainty;

    @Column(name = "uncertainty_type")
    private String uncertaintyType;

    @Column(name = "method")
    private String method;

    @Column(name = "assumptions", columnDefinition = "TEXT")
    private String assumptions;

    @Column(name = "covariance_data", columnDefinition = "TEXT")
    private String covarianceData;

    @Column(name = "validation_status")
    private String validationStatus = "STORED"; // Default to stored, separate from VALIDATED

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getExperimentId() { return experimentId; }
    public void setExperimentId(UUID experimentId) { this.experimentId = experimentId; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Double getUncertainty() { return uncertainty; }
    public void setUncertainty(Double uncertainty) { this.uncertainty = uncertainty; }

    public String getUncertaintyType() { return uncertaintyType; }
    public void setUncertaintyType(String uncertaintyType) { this.uncertaintyType = uncertaintyType; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getAssumptions() { return assumptions; }
    public void setAssumptions(String assumptions) { this.assumptions = assumptions; }

    public String getCovarianceData() { return covarianceData; }
    public void setCovarianceData(String covarianceData) { this.covarianceData = covarianceData; }

    public String getValidationStatus() { return validationStatus; }
    public void setValidationStatus(String validationStatus) { this.validationStatus = validationStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
