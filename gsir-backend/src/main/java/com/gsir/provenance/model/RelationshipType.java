package com.gsir.provenance.model;

/**
 * Part 80 (80.7) — Provenance Edge Relationship Types.
 * Describes directed relationships between provenance nodes.
 */
public enum RelationshipType {
    // Core W3C PROV-O types
    USED,                 // Activity USED Artifact (input)
    WAS_GENERATED_BY,     // Artifact WAS_GENERATED_BY Activity (output)
    WAS_ASSOCIATED_WITH,  // Activity WAS_ASSOCIATED_WITH Agent (actor)
    WAS_DERIVED_FROM,     // Artifact WAS_DERIVED_FROM Artifact (lineage)

    // Part 80.7 additional types
    EXECUTED_BY,          // Artifact/Result EXECUTED_BY Activity/Agent
    VALIDATED_BY,         // Artifact VALIDATED_BY Activity/Agent
    TRANSFORMED_BY,       // Artifact TRANSFORMED_BY Activity
    CONFIGURED_BY,        // Activity CONFIGURED_BY Parameter/Artifact
    VERSION_OF,           // Artifact VERSION_OF Artifact (versioning)
    REVIEWED_BY,          // Artifact REVIEWED_BY Agent (human review)
    SUPPORTED_BY,         // Claim SUPPORTED_BY Evidence
    CONTRADICTED_BY,      // Claim CONTRADICTED_BY Evidence
    EXTRACTED_FROM        // Artifact EXTRACTED_FROM Source/Document
}

