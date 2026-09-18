package com.gsir.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "OutboxEvent")
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 200)
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "published_at")
    private Instant publishedAt;

    @jakarta.persistence.Column(name = "request_id", length = 100)
    private String requestId;

    @jakarta.persistence.Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

        @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "QUEUED";

    protected OutboxEvent() {}

    private OutboxEvent(UUID id, String aggregateType, UUID aggregateId,
                        String eventType, String payload, String requestId, String traceId) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.requestId = requestId;
        this.traceId = traceId;
        this.createdAt = Instant.now();
    }

    public static OutboxEvent projectCreated(UUID id, com.gsir.projects.ProjectEntity project) {
        String payload = String.format(
                "{\"projectId\":\"%s\",\"workspaceId\":\"%s\",\"name\":\"%s\"}",
                project.getId(),
                project.getWorkspaceId(),
                project.getName()
        );
        return new OutboxEvent(id, "Project", project.getId(), "com.gsir.projects.ProjectCreated", payload, org.slf4j.MDC.get("requestId"), org.slf4j.MDC.get("traceId"));
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public String getRequestId() { return requestId; }
    public String getTraceId() { return traceId; }

        public int getAttempts() { return attempts; }
    public String getLastError() { return lastError; }
    public String getStatus() { return status; }

    public void markPublished() {
        this.publishedAt = Instant.now();
        this.status = "PUBLISHED";
    }

    public void recordFailure(String error) {
        this.attempts++;
        this.lastError = error;
        if (this.attempts >= 5) {
            this.status = "DEAD_LETTER";
        }
    }
}


