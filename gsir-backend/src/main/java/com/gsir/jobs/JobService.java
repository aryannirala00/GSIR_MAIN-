package com.gsir.jobs;

import com.gsir.common.error.CommonErrors;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.common.idempotency.IdempotencyRecordEntity;
import com.gsir.common.idempotency.IdempotencyRecordId;
import com.gsir.common.idempotency.IdempotencyRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobAttemptRepository jobAttemptRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final UuidV7Generator idGenerator;
    private final TransactionTemplate transactionTemplate;

    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public JobService(org.springframework.context.ApplicationEventPublisher eventPublisher, 
            JobRepository jobRepository,
            JobAttemptRepository jobAttemptRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            UuidV7Generator idGenerator,
            TransactionTemplate transactionTemplate) {
        this.jobRepository = jobRepository;
        this.jobAttemptRepository = jobAttemptRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.idGenerator = idGenerator;
        this.transactionTemplate = transactionTemplate;
        this.eventPublisher = eventPublisher;
    }

    public JobEntity submitJob(UUID userId, String idempotencyKey, String jobType, String payload) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecordId recordId = new IdempotencyRecordId(userId, idempotencyKey);
            Optional<IdempotencyRecordEntity> existingRecord = idempotencyRecordRepository.findById(recordId);
            if (existingRecord.isPresent()) {
                return jobRepository.findById(existingRecord.get().getResourceId())
                        .orElseThrow(() -> CommonErrors.notFound("Job"));
            }
        }
        
        try {
            return transactionTemplate.execute(status -> {
                UUID jobId = idGenerator.generate();
                
                if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                    idempotencyRecordRepository.saveAndFlush(
                            new IdempotencyRecordEntity(new IdempotencyRecordId(userId, idempotencyKey), "Job", jobId)
                    );
                }
                
                JobEntity job = new JobEntity(jobId, jobType, payload, Instant.now(), org.slf4j.MDC.get("requestId"), org.slf4j.MDC.get("traceId"));
                return jobRepository.save(job);
            });
        } catch (DataIntegrityViolationException e) {
            if (idempotencyKey != null) {
                return idempotencyRecordRepository
                        .findById(new IdempotencyRecordId(userId, idempotencyKey))
                        .flatMap(rec -> jobRepository.findById(rec.getResourceId()))
                        .orElseThrow(() -> CommonErrors.notFound("Job"));
            }
            throw CommonErrors.conflict("Concurrent conflict during job submission");
        }
    }
    
    @Transactional
    public JobAttemptEntity startAttempt(JobEntity job) {
        job.setStatus(JobStatus.RUNNING);
        job.incrementAttempts();
        jobRepository.save(job);
        
        JobAttemptEntity attempt = new JobAttemptEntity(idGenerator.generate(), job.getId(), job.getAttempts());
        return jobAttemptRepository.save(attempt);
    }
    
    @Transactional
    public void markAttemptCompleted(JobEntity job, JobAttemptEntity attempt) {
        job.setStatus(JobStatus.COMPLETED);
        jobRepository.save(job);
        
        attempt.markCompleted();
        jobAttemptRepository.save(attempt);
    }
    
    @Transactional
    public void markAttemptFailed(JobEntity job, JobAttemptEntity attempt, String errorMessage, int maxAttempts) {
        attempt.markFailed(errorMessage);
        jobAttemptRepository.save(attempt);
        
        if (job.getAttempts() >= maxAttempts) {
            job.setStatus(JobStatus.FAILED);
            eventPublisher.publishEvent(new JobFailedEvent(this, job));
        } else {
            job.setStatus(JobStatus.QUEUED);
            // Exponential backoff
            long delaySeconds = (long) Math.pow(2, job.getAttempts()) * 5; 
            job.setAvailableAt(Instant.now().plusSeconds(delaySeconds));
        }
        jobRepository.save(job);
    }

    @Transactional
    public java.util.List<JobAttemptEntity> claimAndStartBatch(int limit) {
        java.util.List<JobEntity> jobs = jobRepository.claimJobs(limit);
        java.util.List<JobAttemptEntity> attempts = new java.util.ArrayList<>();
        for (JobEntity job : jobs) {
            attempts.add(startAttempt(job));
        }
        return attempts;
    }
}

