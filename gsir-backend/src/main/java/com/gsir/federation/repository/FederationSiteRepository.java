package com.gsir.federation.repository;

import com.gsir.federation.model.FederationSiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FederationSiteRepository extends JpaRepository<FederationSiteEntity, UUID> {
    Optional<FederationSiteEntity> findByName(String name);
}

