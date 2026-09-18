package com.gsir.retrieval;

import java.util.List;

public interface RetrievalService {
    List<RetrievalResult> retrieve(RetrievalQuery query);
    List<RetrievalResult> retrieveComplex(String userIntent, java.util.UUID projectId);
}
