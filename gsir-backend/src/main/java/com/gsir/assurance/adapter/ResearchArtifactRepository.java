package com.gsir.assurance.adapter;

import com.gsir.assurance.model.ResearchArtifactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResearchArtifactRepository extends JpaRepository<ResearchArtifactEntity, UUID> {
    List<ResearchArtifactEntity> findByProjectId(UUID projectId);
    java.util.Optional<ResearchArtifactEntity> findByIdAndProjectId(UUID id, UUID projectId);
}

