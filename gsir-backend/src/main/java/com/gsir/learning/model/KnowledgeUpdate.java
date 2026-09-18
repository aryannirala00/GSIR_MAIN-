package com.gsir.learning.model;

import com.gsir.knowledge.model.ClaimType;
import java.util.UUID;

public record KnowledgeUpdate(
    UUID subjectId,
    String predicate,
    UUID objectId,
    String statement,
    ClaimType type,
    UUID evidenceDocumentId,
    Double confidence
) {}

