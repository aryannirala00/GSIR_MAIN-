package com.gsir.data.model;

import com.gsir.common.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "dataset_licenses")
public class DatasetLicenseEntity extends AuditedEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "dataset_id", nullable = false)
    private UUID datasetId;

    @Column(name = "license_type", nullable = false, length = 64)
    private String licenseType;

    @Column(name = "usage_restrictions")
    private String usageRestrictions;

    @Column(name = "attribution_requirements")
    private String attributionRequirements;

    @Column(name = "redistribution_rules")
    private String redistributionRules;

    @Column(name = "commercial_restrictions")
    private String commercialRestrictions;

    protected DatasetLicenseEntity() {}

    public DatasetLicenseEntity(UUID id, UUID datasetId, String licenseType) {
        this.id = id;
        this.datasetId = datasetId;
        this.licenseType = licenseType;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDatasetId() {
        return datasetId;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public String getUsageRestrictions() {
        return usageRestrictions;
    }

    public String getAttributionRequirements() {
        return attributionRequirements;
    }

    public String getRedistributionRules() {
        return redistributionRules;
    }

    public String getCommercialRestrictions() {
        return commercialRestrictions;
    }
}

