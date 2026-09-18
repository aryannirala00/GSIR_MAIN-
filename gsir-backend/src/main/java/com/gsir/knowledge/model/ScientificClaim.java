package com.gsir.knowledge.model;

import java.util.UUID;
import java.util.List;

public record ScientificClaim(
    UUID claimId,
    String statement,
    ClaimType claimType,
    UUID subjectEntityId,
    String predicate,
    UUID objectEntityId,
    List<EvidenceLink> evidence,
    List<UUID> sourceIds,
    UUID provenanceId,
    TemporalProperties temporalContext,
    String domain,
    Double confidence,
    ClaimStatus status,
    ValidationStatus validationStatus,
    UUID projectId,
    Integer version
) {}
