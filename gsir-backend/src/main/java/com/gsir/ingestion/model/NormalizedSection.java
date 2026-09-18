package com.gsir.ingestion.model;

public record NormalizedSection(
    String sectionId,
    String title,
    String content,
    String sourceLocation
) {}
