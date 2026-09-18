package com.gsir.files;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    private UUID id;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "ACTIVE";

        @Column(name = "processing_status", nullable = false, length = 32)
    private String processingStatus = "READY";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FileEntity() {}

    public FileEntity(UUID id, UUID projectId, UUID userId, String name, String mimeType, Long sizeBytes) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.name = name;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.status = "ACTIVE";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getMimeType() { return mimeType; }
    public Long getSizeBytes() { return sizeBytes; }
        public String getStatus() { return status; }
    public String getProcessingStatus() { return processingStatus; }
    public Instant getCreatedAt() { return createdAt; }
    
    public void setStatus(String status) { this.status = status; }
    public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }
    public void setName(String name) { this.name = name; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
}


