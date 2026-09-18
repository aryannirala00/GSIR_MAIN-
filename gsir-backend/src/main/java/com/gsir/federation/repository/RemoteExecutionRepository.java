package com.gsir.federation.repository;

import com.gsir.federation.model.RemoteExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RemoteExecutionRepository extends JpaRepository<RemoteExecutionEntity, UUID> {
    List<RemoteExecutionEntity> findByWorkflowId(UUID workflowId);
    List<RemoteExecutionEntity> findByTargetSiteId(UUID targetSiteId);
}

