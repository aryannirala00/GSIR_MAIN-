package com.gsir.notes;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "NoteRelationship")
@Table(name = "note_relationships")
public class NoteRelationshipEntity {

    @Id
    private UUID id;

    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @Column(name = "target_type", nullable = false, length = 100)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected NoteRelationshipEntity() {}

    public NoteRelationshipEntity(UUID id, UUID noteId, String targetType, UUID targetId) {
        this.id = id;
        this.noteId = noteId;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public UUID getNoteId() { return noteId; }
    public String getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public Instant getCreatedAt() { return createdAt; }
}

