package com.gsir.retrieval;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoopReranker implements Reranker {
    @Override
    public List<RetrievalResult> rerank(String originalQuery, List<RetrievalResult> candidates, int topK) {
        return candidates.stream().limit(topK).collect(Collectors.toList());
    }
}
