package com.gsir.execution.adapter;

import com.gsir.execution.model.HumanReviewTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HumanReviewTaskRepository extends JpaRepository<HumanReviewTaskEntity, UUID> {
    List<HumanReviewTaskEntity> findByProjectId(UUID projectId);
    List<HumanReviewTaskEntity> findByWorkflowId(UUID workflowId);
    Optional<HumanReviewTaskEntity> findByIdAndProjectId(UUID id, UUID projectId);
}
