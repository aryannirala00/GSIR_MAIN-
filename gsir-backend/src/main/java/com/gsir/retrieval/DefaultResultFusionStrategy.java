package com.gsir.retrieval;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DefaultResultFusionStrategy implements ResultFusionStrategy {

    @Override
    public List<RetrievalResult> fuse(List<List<RetrievalResult>> candidateSets) {
        List<RetrievalResult> combined = new ArrayList<>();
        Set<String> seenResourceIds = new HashSet<>();

        for (List<RetrievalResult> set : candidateSets) {
            for (RetrievalResult result : set) {
                // Basic deduplication
                if (result.resourceId() != null && seenResourceIds.add(result.resourceId())) {
                    combined.add(result);
                } else if (result.resourceId() == null) {
                    combined.add(result);
                }
            }
        }

        // Simple score-based ranking
        combined.sort(Comparator.comparingDouble(RetrievalResult::score).reversed());
        return combined;
    }
}
