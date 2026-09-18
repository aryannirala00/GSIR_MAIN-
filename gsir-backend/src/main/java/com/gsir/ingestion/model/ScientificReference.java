package com.gsir.ingestion.model;

public record ScientificReference(
    String referenceId,
    String citationText,
    String referencedIdentifier,
    java.util.List<String> authors,
    String title,
    String publication,
    Integer year,
    String sourceLocation
) {}
