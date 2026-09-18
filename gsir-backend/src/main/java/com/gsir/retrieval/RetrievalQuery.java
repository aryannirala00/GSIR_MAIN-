package com.gsir.retrieval;

import java.util.Map;
import java.util.UUID;

public record RetrievalQuery(
    String queryId,
    String text,
    RetrievalStrategy strategy,
    Map<String, Object> filters,
    UUID projectId,
    int topK
) {}
