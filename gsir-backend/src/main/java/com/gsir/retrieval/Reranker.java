package com.gsir.retrieval;

import java.util.List;

public interface Reranker {
    List<RetrievalResult> rerank(String originalQuery, List<RetrievalResult> candidates, int topK);
}
