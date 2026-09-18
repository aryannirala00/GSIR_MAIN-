package com.gsir.files;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "storage_objects")
public class StorageObjectEntity {

    @Id
    private UUID id;

    @Column(name = "file_version_id", nullable = false)
    private UUID fileVersionId;

    @Column(name = "provider", nullable = false, length = 64)
    private String provider;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StorageObjectEntity() {}

    public StorageObjectEntity(UUID id, UUID fileVersionId, String provider, String objectKey, String checksumSha256) {
        this.id = id;
        this.fileVersionId = fileVersionId;
        this.provider = provider;
        this.objectKey = objectKey;
        this.checksumSha256 = checksumSha256;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public UUID getFileVersionId() { return fileVersionId; }
    public String getProvider() { return provider; }
    public String getObjectKey() { return objectKey; }
    public String getChecksumSha256() { return checksumSha256; }
    public Instant getCreatedAt() { return createdAt; }
}

