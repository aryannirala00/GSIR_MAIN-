package com.gsir.metadata;

public record CreateMetadataRequest(
    String objectType,
    String persistentIdentifier,
    String versionId,
    String metadata,
    String schemaId
) {}
