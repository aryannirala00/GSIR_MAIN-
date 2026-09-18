package com.gsir.provenance.model;

import java.time.Instant;
import java.util.UUID;

public record ProvRelationship(
    UUID id,
    UUID projectId,
    RelationshipType relationshipType,
    UUID sourceId,
    UUID targetId,
    Instant createdAt
) {}
