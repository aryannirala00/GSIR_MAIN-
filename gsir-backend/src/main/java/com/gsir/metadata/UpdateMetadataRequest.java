package com.gsir.metadata;

public record UpdateMetadataRequest(
    String metadata,
    String versionId
) {}
