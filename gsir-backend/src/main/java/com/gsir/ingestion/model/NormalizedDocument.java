package com.gsir.ingestion.model;

import java.util.List;

public record NormalizedDocument(
    java.util.UUID documentId,
    java.util.Map<String, String> metadata,
    String fullText,
    List<NormalizedSection> sections,
    List<NormalizedTable> tables,
    List<NormalizedFigure> figures,
    List<NormalizedEquation> equations,
    List<ScientificReference> references,
    java.util.UUID projectId
) {}
