package com.gsir.knowledge.adapter;
import com.gsir.knowledge.model.OntologyEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface OntologyRepository extends JpaRepository<OntologyEntity, UUID> {
    Optional<OntologyEntity> findByProjectIdAndNamespaceAndVersion(UUID projectId, String namespace, String version);
    List<OntologyEntity> findByProjectId(UUID projectId);
}

