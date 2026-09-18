package com.gsir.ingestion.model;

public record NormalizedFigure(
    String figureId,
    String caption,
    String sourceLocation,
    java.util.UUID imageId
) {}
