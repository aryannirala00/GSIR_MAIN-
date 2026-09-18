package com.gsir.knowledge.model;

import java.util.UUID;
import java.util.List;
import java.util.Map;

public record KnowledgeRelationship(
    UUID relationshipId,
    String relationshipType,
    UUID sourceEntityId,
    UUID targetEntityId,
    Map<String, Object> properties,
    Double confidence,
    UUID provenanceId,
    List<EvidenceLink> evidence,
    TemporalProperties temporal,
    ValidationStatus validationStatus,
    String status,
    UUID projectId,
    Integer version,
    boolean isInferred,
    Map<String, Object> inferenceProvenance
) {}

