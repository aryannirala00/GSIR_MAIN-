package com.gsir.notes;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "NoteVersion")
@Table(name = "note_versions")
public class NoteVersionEntity {

    @Id
    private UUID id;

    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    protected NoteVersionEntity() {}

    public NoteVersionEntity(UUID id, UUID noteId, String title, String content, Integer versionNumber, UUID createdBy) {
        this.id = id;
        this.noteId = noteId;
        this.title = title;
        this.content = content;
        this.versionNumber = versionNumber;
        this.createdBy = createdBy;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public UUID getNoteId() { return noteId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Integer getVersionNumber() { return versionNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getCreatedBy() { return createdBy; }
}

