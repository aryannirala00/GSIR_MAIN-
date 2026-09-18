package com.gsir.experiments.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "experiment_observations")
public class ExperimentObservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "variable_id")
    private UUID variableId;

    @Column(name = "\"timestamp\"", nullable = false)
    private Instant timestamp;

    @Column(name = "\"value\"", nullable = false)
    private String value;

    private String unit;
    private String source;

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

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public UUID getVariableId() { return variableId; }
    public void setVariableId(UUID variableId) { this.variableId = variableId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
