package com.gsir.provenance.model;

/**
 * Part 80 (80.24/80.84) — Actor and Provenance Confidence types.
 */
public enum ProvenanceConfidence {
    AUTOMATIC,      // Captured automatically during execution
    VERIFIED,       // Independently verified
    RECONSTRUCTED,  // Reconstructed from available evidence after the fact
    INFERRED,       // Inferred from context
    UNKNOWN         // Provenance is missing or cannot be determined
}

