package com.gsir.jobs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, UUID> {
    
    @Query(value = "SELECT * FROM jobs WHERE status = 'QUEUED' AND available_at <= NOW() ORDER BY available_at ASC LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<JobEntity> claimJobs(@Param("limit") int limit);

    long countByStatus(JobStatus status);
}


