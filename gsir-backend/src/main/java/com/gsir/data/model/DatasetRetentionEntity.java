package com.gsir.data.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dataset_retention")
public class DatasetRetentionEntity {
    @Id
    private UUID id;

    @Column(name = "dataset_id", nullable = false)
    private UUID datasetId;

    @Column(name = "retention_period_days")
    private Integer retentionPeriodDays;

    @Column(name = "archive_date")
    private Instant archiveDate;

    @Column(name = "deletion_date")
    private Instant deletionDate;

    @Column(name = "legal_hold")
    private Boolean legalHold = false;

    @Column(name = "research_hold")
    private Boolean researchHold = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDatasetId() { return datasetId; }
    public void setDatasetId(UUID datasetId) { this.datasetId = datasetId; }
    public Integer getRetentionPeriodDays() { return retentionPeriodDays; }
    public void setRetentionPeriodDays(Integer retentionPeriodDays) { this.retentionPeriodDays = retentionPeriodDays; }
    public Instant getArchiveDate() { return archiveDate; }
    public void setArchiveDate(Instant archiveDate) { this.archiveDate = archiveDate; }
    public Instant getDeletionDate() { return deletionDate; }
    public void setDeletionDate(Instant deletionDate) { this.deletionDate = deletionDate; }
    public Boolean getLegalHold() { return legalHold; }
    public void setLegalHold(Boolean legalHold) { this.legalHold = legalHold; }
    public Boolean getResearchHold() { return researchHold; }
    public void setResearchHold(Boolean researchHold) { this.researchHold = researchHold; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
