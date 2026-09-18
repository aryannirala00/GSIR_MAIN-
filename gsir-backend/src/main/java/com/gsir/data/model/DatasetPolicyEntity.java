package com.gsir.data.model;

import com.gsir.common.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "dataset_policies")
public class DatasetPolicyEntity extends AuditedEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "dataset_id", nullable = false)
    private UUID datasetId;

    @Column(name = "policy_name", nullable = false, length = 100)
    private String policyName;

    @Column(name = "policy_type", nullable = false, length = 64)
    private String policyType;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String rules;

    @Column(name = "enforcement_level", nullable = false, length = 32)
    private String enforcementLevel;

    protected DatasetPolicyEntity() {}

    public DatasetPolicyEntity(UUID id, UUID datasetId, String policyName, String policyType, String rules, String enforcementLevel) {
        this.id = id;
        this.datasetId = datasetId;
        this.policyName = policyName;
        this.policyType = policyType;
        this.rules = rules;
        this.enforcementLevel = enforcementLevel;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDatasetId() {
        return datasetId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getPolicyType() {
        return policyType;
    }

    public String getRules() {
        return rules;
    }

    public String getEnforcementLevel() {
        return enforcementLevel;
    }
}

