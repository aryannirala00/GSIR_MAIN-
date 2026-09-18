package com.gsir.notes;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "KnowledgeCandidate")
@Table(name = "knowledge_candidates")
public class KnowledgeCandidateEntity {

    @Id
    private UUID id;

    @Column(name = "source_note_id", nullable = false)
    private UUID sourceNoteId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String provenance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    protected KnowledgeCandidateEntity() {}

    public KnowledgeCandidateEntity(UUID id, UUID sourceNoteId, String content, String status, String provenance, UUID createdBy) {
        this.id = id;
        this.sourceNoteId = sourceNoteId;
        this.content = content;
        this.status = status;
        this.provenance = provenance;
        this.createdBy = createdBy;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public UUID getSourceNoteId() { return sourceNoteId; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProvenance() { return provenance; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getCreatedBy() { return createdBy; }
}

