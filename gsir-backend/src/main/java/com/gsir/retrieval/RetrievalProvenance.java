package com.gsir.retrieval;

import java.time.Instant;

public record RetrievalProvenance(
    String url,
    String title,
    String publisher,
    Instant retrievalTime,
    Instant publicationTime,
    String contentReference
) {}
