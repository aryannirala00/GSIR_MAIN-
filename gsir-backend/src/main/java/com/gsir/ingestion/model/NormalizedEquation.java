package com.gsir.ingestion.model;

public record NormalizedEquation(
    String equationId,
    String expression,
    String format,
    String contextText,
    String sourceLocation
) {}
