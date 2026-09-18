package com.gsir.data.repository;

import com.gsir.data.model.DatasetVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface DatasetVersionRepository extends JpaRepository<DatasetVersionEntity, UUID> {
    List<DatasetVersionEntity> findByDatasetId(UUID datasetId);
}
