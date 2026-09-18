package com.gsir.resources;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ResourceQuotaRepository extends JpaRepository<ResourceQuotaEntity, UUID> {

    /**
     * Fetch the quota row with a PESSIMISTIC_WRITE lock so that concurrent
     * admission checks for the same project are serialised at the database level.
     * <p>
     * NOTE: This provides database-level serialisation on PostgreSQL (SELECT … FOR UPDATE).
     * H2 in-memory tests execute the query without locking; H2 tests verify functional
     * correctness only, NOT PostgreSQL locking semantics.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM ResourceQuota q WHERE q.projectId = :projectId")
    Optional<ResourceQuotaEntity> findByIdForUpdate(@Param("projectId") UUID projectId);
}
