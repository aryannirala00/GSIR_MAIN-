package com.gsir.metadata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ResearchObjectRepository extends JpaRepository<ResearchObjectEntity, UUID> {
    Optional<ResearchObjectEntity> findByIdAndProjectId(UUID id, UUID projectId);
    List<ResearchObjectEntity> findByProjectId(UUID projectId);
}
