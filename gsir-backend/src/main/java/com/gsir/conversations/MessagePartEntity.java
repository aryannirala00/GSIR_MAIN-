package com.gsir.conversations;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "message_parts")
public class MessagePartEntity {
    @Id
    private UUID id;
    
    @Column(name = "message_id", nullable = false)
    private UUID messageId;
    
    @Column(name = "part_type", length = 32, nullable = false)
    private String partType;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "jsonb")
    private String metadata;
    
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    public MessagePartEntity() {}

    public MessagePartEntity(UUID id, UUID messageId, String partType, String content, String metadata, Integer sequenceNumber) {
        this.id = id;
        this.messageId = messageId;
        this.partType = partType;
        this.content = content;
        this.metadata = metadata;
        this.sequenceNumber = sequenceNumber;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }
    public String getPartType() { return partType; }
    public void setPartType(String partType) { this.partType = partType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
}

