package com.gsir.metadata;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "metadata_schemas")
public class MetadataSchemaEntity {

    @Id
    private UUID id;

    @Column(name = "schema_name", nullable = false)
    private String schemaName;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_definition", columnDefinition = "jsonb", nullable = false)
    private String schemaDefinition;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public MetadataSchemaEntity() {}

    public MetadataSchemaEntity(UUID id, String schemaName, String schemaVersion, String schemaDefinition) {
        this.id = id;
        this.schemaName = schemaName;
        this.schemaVersion = schemaVersion;
        this.schemaDefinition = schemaDefinition;
        this.createdAt = Instant.now();
    }
    
    public UUID getId() { return id; }
    public String getSchemaName() { return schemaName; }
    public String getSchemaVersion() { return schemaVersion; }
    public String getSchemaDefinition() { return schemaDefinition; }
}
