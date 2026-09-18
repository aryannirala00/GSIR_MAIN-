package com.gsir.data.repository;

import com.gsir.data.model.DatasetShareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface DatasetShareRepository extends JpaRepository<DatasetShareEntity, UUID> {
    List<DatasetShareEntity> findByDatasetId(UUID datasetId);
}
