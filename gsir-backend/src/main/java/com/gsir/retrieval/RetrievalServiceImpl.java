package com.gsir.retrieval;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class RetrievalServiceImpl implements RetrievalService {

    private final List<SearchAdapter> adapters;
    private final ResultFusionStrategy fusionStrategy;
    private final Reranker reranker;

    public RetrievalServiceImpl(List<SearchAdapter> adapters, ResultFusionStrategy fusionStrategy, Reranker reranker) {
        this.adapters = adapters;
        this.fusionStrategy = fusionStrategy;
        this.reranker = reranker;
    }

    @Override
    public List<RetrievalResult> retrieve(RetrievalQuery query) {
        // Find matching adapters (could be multiple for HYBRID)
        List<SearchAdapter> matchingAdapters = adapters.stream()
                .filter(a -> a.supports(query.strategy()))
                .collect(Collectors.toList());

        if (matchingAdapters.isEmpty()) {
            return List.of();
        }

        // Parallel retrieval
        List<List<RetrievalResult>> candidateSets = matchingAdapters.parallelStream()
                .map(a -> a.search(query))
                .collect(Collectors.toList());

        // Fusion & Deduplication
        List<RetrievalResult> fused = fusionStrategy.fuse(candidateSets);

        // Optional Reranking
        if (reranker != null && fused.size() > query.topK()) {
            return reranker.rerank(query.text(), fused, query.topK());
        }

        // Truncate to topK if no reranker
        return fused.stream().limit(query.topK()).collect(Collectors.toList());
    }

    @Override
    public List<RetrievalResult> retrieveComplex(String userIntent, UUID projectId) {
        // Query understanding / Decomposition stub
        RetrievalQuery initialQuery = new RetrievalQuery(
                UUID.randomUUID().toString(),
                userIntent,
                RetrievalStrategy.HYBRID,
                Map.of("quality", "peer-reviewed"),
                projectId,
                10
        );
        return retrieve(initialQuery);
    }
}
