package com.gsir.processing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentExtractionRepository extends JpaRepository<DocumentExtractionEntity, UUID> {
    List<DocumentExtractionEntity> findByFileVersionId(UUID fileVersionId);
}
