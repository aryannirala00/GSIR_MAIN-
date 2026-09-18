package com.gsir.resources;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity(name = "ResourceQuota")
@Table(name = "resource_quotas")
public class ResourceQuotaEntity {

    @Id
    @Column(nullable = false)
    private UUID projectId;

    @Column(name = "max_compute_hours")
    private Double maxComputeHours;

    @Column(name = "max_ai_tokens")
    private Long maxAiTokens;

    @Column(name = "max_parallel_jobs")
    private Integer maxParallelJobs;

    @Column(name = "max_storage_bytes")
    private Long maxStorageBytes;

    protected ResourceQuotaEntity() {}

    public ResourceQuotaEntity(UUID projectId, Double maxComputeHours, Long maxAiTokens, Integer maxParallelJobs, Long maxStorageBytes) {
        this.projectId = projectId;
        this.maxComputeHours = maxComputeHours;
        this.maxAiTokens = maxAiTokens;
        this.maxParallelJobs = maxParallelJobs;
        this.maxStorageBytes = maxStorageBytes;
    }

    public UUID getProjectId() { return projectId; }
    public Double getMaxComputeHours() { return maxComputeHours; }
    public Long getMaxAiTokens() { return maxAiTokens; }
    public Integer getMaxParallelJobs() { return maxParallelJobs; }
    public Long getMaxStorageBytes() { return maxStorageBytes; }
}
