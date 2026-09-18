package com.gsir.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ResearchWorkflowRepository extends JpaRepository<ResearchWorkflowEntity, UUID> {
    List<ResearchWorkflowEntity> findByProjectId(UUID projectId);
    
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT w FROM ResearchWorkflowEntity w WHERE w.id = :id")
    java.util.Optional<ResearchWorkflowEntity> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") UUID id);
}
