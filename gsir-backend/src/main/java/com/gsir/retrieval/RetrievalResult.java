package com.gsir.retrieval;

import java.util.Map;

public record RetrievalResult(
    String resourceId,
    String sourceType,
    String content,
    double score,
    Map<String, Object> metadata,
    RetrievalProvenance provenance,
    String accessScope
) {}
