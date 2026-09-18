package com.gsir.assurance.adapter;

import com.gsir.assurance.model.AssuranceAttestationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssuranceAttestationRepository extends JpaRepository<AssuranceAttestationEntity, UUID> {
    List<AssuranceAttestationEntity> findByArtifactId(UUID artifactId);
}
