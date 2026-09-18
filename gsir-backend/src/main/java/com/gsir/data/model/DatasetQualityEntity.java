package com.gsir.data.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dataset_quality")
public class DatasetQualityEntity {
    @Id
    private UUID id;

    @Column(name = "dataset_id", nullable = false)
    private UUID datasetId;

    private Double completeness;
    private Double accuracy;
    private Double consistency;
    private Double validity;
    private Double uniqueness;
    private Double timeliness;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDatasetId() { return datasetId; }
    public void setDatasetId(UUID datasetId) { this.datasetId = datasetId; }
    public Double getCompleteness() { return completeness; }
    public void setCompleteness(Double completeness) { this.completeness = completeness; }
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
    public Double getConsistency() { return consistency; }
    public void setConsistency(Double consistency) { this.consistency = consistency; }
    public Double getValidity() { return validity; }
    public void setValidity(Double validity) { this.validity = validity; }
    public Double getUniqueness() { return uniqueness; }
    public void setUniqueness(Double uniqueness) { this.uniqueness = uniqueness; }
    public Double getTimeliness() { return timeliness; }
    public void setTimeliness(Double timeliness) { this.timeliness = timeliness; }
    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
