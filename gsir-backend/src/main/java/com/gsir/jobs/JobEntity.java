package com.gsir.jobs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity(name = "Job")
@Table(name = "jobs")
public class JobEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "job_type", nullable = false, length = 100)
    private String jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private JobStatus status;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @jakarta.persistence.Column(name = "request_id", length = 100)
    private String requestId;

    @jakarta.persistence.Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected JobEntity() {}

    public JobEntity(UUID id, String jobType, String payload, Instant availableAt, String requestId, String traceId) {
        this.requestId = requestId;
        this.traceId = traceId;
        this.id = id;
        this.jobType = jobType;
        this.status = JobStatus.QUEUED;
        this.payload = payload;
        this.attempts = 0;
        this.availableAt = availableAt != null ? availableAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = createdAt;
        if (availableAt == null) {
            availableAt = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getJobType() { return jobType; }
    public JobStatus getStatus() { return status; }
    public String getPayload() { return payload; }
    public int getAttempts() { return attempts; }
    public Instant getAvailableAt() { return availableAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getRequestId() { return requestId; }
    public String getTraceId() { return traceId; }

    public void setStatus(JobStatus status) { this.status = status; }
    public void incrementAttempts() { this.attempts++; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }

    @Override
    @Transient
    public boolean isNew() {
        return createdAt == null;
    }
}
