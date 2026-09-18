package com.gsir.jobs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class JobSystemTest {

    @Autowired
    private JobService jobService;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private JobAttemptRepository jobAttemptRepository;

    @Test
    @Transactional
    public void testJobCreationAndAttempt() {
        UUID userId = UUID.randomUUID();
        String payload = "{\"tenantId\":\"" + userId + "\"}";
        
        JobEntity job = jobService.submitJob(userId, "key123", "TEST_JOB", payload);
        
        assertThat(job).isNotNull();
        assertThat(job.getId()).isNotNull();
        assertThat(job.getStatus()).isEqualTo(JobStatus.QUEUED);
        
        List<JobEntity> claimed = jobRepository.claimJobs(5);
        assertThat(claimed).hasSize(1);
        
        JobAttemptEntity attempt = jobService.startAttempt(claimed.get(0));
        assertThat(attempt).isNotNull();
        assertThat(attempt.getStatus()).isEqualTo(JobAttemptStatus.RUNNING);
        
        jobService.markAttemptCompleted(claimed.get(0), attempt);
        
        JobEntity updatedJob = jobRepository.findById(job.getId()).get();
        assertThat(updatedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
    }
}
