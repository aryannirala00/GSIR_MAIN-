package com.gsir.processing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_extractions")
public class DocumentExtractionEntity {

    @Id
    private UUID id;

    @Column(name = "file_version_id", nullable = false)
    private UUID fileVersionId;

    @Column(name = "extraction_type", nullable = false, length = 64)
    private String extractionType;

    @Column(name = "content")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected DocumentExtractionEntity() {}

    public DocumentExtractionEntity(UUID id, UUID fileVersionId, String extractionType, String content, String metadata) {
        this.id = id;
        this.fileVersionId = fileVersionId;
        this.extractionType = extractionType;
        this.content = content;
        this.metadata = metadata;
    }

    public UUID getId() { return id; }
    public UUID getFileVersionId() { return fileVersionId; }
    public String getExtractionType() { return extractionType; }
    public String getContent() { return content; }
    public String getMetadata() { return metadata; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
