package com.gsir.notes;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "NoteAttachment")
@Table(name = "note_attachments")
public class NoteAttachmentEntity {

    @Id
    private UUID id;

    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected NoteAttachmentEntity() {}

    public NoteAttachmentEntity(UUID id, UUID noteId, UUID fileId) {
        this.id = id;
        this.noteId = noteId;
        this.fileId = fileId;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public UUID getNoteId() { return noteId; }
    public UUID getFileId() { return fileId; }
    public Instant getCreatedAt() { return createdAt; }
}

