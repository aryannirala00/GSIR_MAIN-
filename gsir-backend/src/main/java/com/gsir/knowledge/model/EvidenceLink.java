package com.gsir.knowledge.model;

import java.util.UUID;

public record EvidenceLink(
    UUID evidenceId,
    UUID sourceDocumentId,
    String exactLocation, // Page, Section, Paragraph, Row
    Double weight,
    String extractionVersion
) {}
