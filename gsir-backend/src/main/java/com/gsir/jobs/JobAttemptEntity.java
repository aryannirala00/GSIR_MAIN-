package com.gsir.jobs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity(name = "JobAttempt")
@Table(name = "job_attempts")
public class JobAttemptEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private JobAttemptStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Transient
    private boolean isNew = true;

    protected JobAttemptEntity() {}

    public JobAttemptEntity(UUID id, UUID jobId, int attemptNumber) {
        this.id = id;
        this.jobId = jobId;
        this.attemptNumber = attemptNumber;
        this.status = JobAttemptStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    @PostLoad
    @PostPersist
    protected void markNotNew() {
        this.isNew = false;
    }

    public UUID getId() { return id; }
    public UUID getJobId() { return jobId; }
    public int getAttemptNumber() { return attemptNumber; }
    public JobAttemptStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getErrorMessage() { return errorMessage; }

    public void markCompleted() {
        this.status = JobAttemptStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = JobAttemptStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    @Override
    @Transient
    public boolean isNew() {
        return isNew;
    }
}
