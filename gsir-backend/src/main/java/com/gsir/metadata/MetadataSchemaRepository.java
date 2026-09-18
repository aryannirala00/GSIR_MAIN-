package com.gsir.metadata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetadataSchemaRepository extends JpaRepository<MetadataSchemaEntity, UUID> {
    Optional<MetadataSchemaEntity> findBySchemaNameAndSchemaVersion(String schemaName, String schemaVersion);
}
