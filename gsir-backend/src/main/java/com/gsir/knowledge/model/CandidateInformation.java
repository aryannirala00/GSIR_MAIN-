package com.gsir.knowledge.model;

import java.util.UUID;
import java.util.List;

public record CandidateInformation(
    String statement,
    ClaimType claimType,
    UUID subjectId,
    String predicate,
    UUID objectId,
    List<EvidenceLink> evidence,
    UUID workflowId,
    UUID stepId,
    UUID projectId,
    UUID userId
) {}

