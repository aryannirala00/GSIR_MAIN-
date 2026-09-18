package com.gsir.files;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_versions")
public class FileVersionEntity {

    @Id
    private UUID id;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FileVersionEntity() {}

    public FileVersionEntity(UUID id, UUID fileId, Integer versionNumber, String checksumSha256, Long sizeBytes) {
        this.id = id;
        this.fileId = fileId;
        this.versionNumber = versionNumber;
        this.checksumSha256 = checksumSha256;
        this.sizeBytes = sizeBytes;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public UUID getFileId() { return fileId; }
    public Integer getVersionNumber() { return versionNumber; }
    public String getChecksumSha256() { return checksumSha256; }
    public Long getSizeBytes() { return sizeBytes; }
    public Instant getCreatedAt() { return createdAt; }
}

