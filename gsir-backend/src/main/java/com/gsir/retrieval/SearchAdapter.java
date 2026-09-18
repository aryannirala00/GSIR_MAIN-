package com.gsir.retrieval;

import java.util.List;

public interface SearchAdapter {
    boolean supports(RetrievalStrategy strategy);
    List<RetrievalResult> search(RetrievalQuery query);
}
