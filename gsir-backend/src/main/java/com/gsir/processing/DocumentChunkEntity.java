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
@Table(name = "document_chunks")
public class DocumentChunkEntity {

    @Id
    private UUID id;

    @Column(name = "file_version_id", nullable = false)
    private UUID fileVersionId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "content", nullable = false)
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected DocumentChunkEntity() {}

    public DocumentChunkEntity(UUID id, UUID fileVersionId, Integer chunkIndex, String content, String metadata) {
        this.id = id;
        this.fileVersionId = fileVersionId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.metadata = metadata;
    }

    public UUID getId() { return id; }
    public UUID getFileVersionId() { return fileVersionId; }
    public Integer getChunkIndex() { return chunkIndex; }
    public String getContent() { return content; }
    public String getMetadata() { return metadata; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
