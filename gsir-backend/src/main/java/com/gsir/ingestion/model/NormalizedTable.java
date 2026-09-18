package com.gsir.ingestion.model;

public record NormalizedTable(
    String tableId,
    String caption,
    java.util.List<String> headers,
    java.util.List<java.util.List<String>> rows,
    String sourceLocation
) {}
