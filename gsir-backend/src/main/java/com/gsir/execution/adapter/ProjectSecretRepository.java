package com.gsir.execution.adapter;

import com.gsir.execution.model.ProjectSecretEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectSecretRepository extends JpaRepository<ProjectSecretEntity, UUID> {
    List<ProjectSecretEntity> findByProjectId(UUID projectId);
    Optional<ProjectSecretEntity> findByProjectIdAndSecretName(UUID projectId, String secretName);
}
