package com.gsir.data.repository;

import com.gsir.data.model.DataObjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataObjectRepository extends JpaRepository<DataObjectEntity, UUID> {
    Page<DataObjectEntity> findByProjectId(UUID projectId, Pageable pageable);
    List<DataObjectEntity> findByProjectIdAndClassification(UUID projectId, String classification);
}

