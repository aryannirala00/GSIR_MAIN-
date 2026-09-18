package com.gsir.retrieval;

import java.util.List;

public interface ResultFusionStrategy {
    List<RetrievalResult> fuse(List<List<RetrievalResult>> candidateSets);
}
