package com.gsir.knowledge.model;

import java.util.UUID;
import java.util.List;
import java.time.Instant;
import java.util.Map;

public record KnowledgeEntity(
    UUID entityId,
    String entityType,
    String canonicalName,
    List<String> aliases,
    String description,
    Map<String, Object> properties,
    String domain,
    List<String> sourceReferences,
    Double confidence,
    ValidationStatus validationStatus,
    String status,
    UUID projectId,
    Instant createdAt,
    Instant updatedAt,
    Integer version
) {}
