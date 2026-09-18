package com.gsir.jobs;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JobWorker {

    private static final Logger log = LoggerFactory.getLogger(JobWorker.class);
    private static final int MAX_ATTEMPTS = 5;

    private final JobRepository jobRepository;
    private final JobService jobService;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public JobWorker(JobRepository jobRepository, JobService jobService, ApplicationEventPublisher eventPublisher, MeterRegistry meterRegistry) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedDelay = 5000)
    public void processJobs() {
        List<JobAttemptEntity> attempts = jobService.claimAndStartBatch(10);
        for (JobAttemptEntity attempt : attempts) {
            processSingleJob(attempt);
        }
    }
    
    private void processSingleJob(JobAttemptEntity attempt) {
        JobEntity job = jobRepository.findById(attempt.getJobId()).orElse(null);
        if (job == null) return;

        long startTime = System.nanoTime();
        String status = "success";
        
        try {
            if (job.getRequestId() != null) org.slf4j.MDC.put("requestId", job.getRequestId());
            if (job.getTraceId() != null) org.slf4j.MDC.put("traceId", job.getTraceId());
        
            log.info("Executing job {} (attempt {})", job.getId(), attempt.getAttemptNumber());
            eventPublisher.publishEvent(new JobExecutionEvent(this, job));
            
            jobService.markAttemptCompleted(job, attempt);
            log.info("Job {} completed successfully", job.getId());
        } catch (Exception e) {
            status = "failure";
            log.error("Job {} failed: {}", job.getId(), e.getMessage(), e);
            jobService.markAttemptFailed(job, attempt, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), MAX_ATTEMPTS);
        } finally {
            long duration = System.nanoTime() - startTime;
            meterRegistry.timer("gsir.jobs.execution", "jobType", job.getJobType(), "status", status).record(duration, TimeUnit.NANOSECONDS);
            
            org.slf4j.MDC.remove("requestId");
            org.slf4j.MDC.remove("traceId");
        }
    }
}
