package com.gsir.ai.memory;

import java.util.List;
import java.util.UUID;

public interface MemoryManager {
    void storeShortTerm(UUID executionId, String key, Object value);
    void storeLongTerm(UUID projectId, String key, Object validatedValue);
    List<Object> retrieveRelevant(String context, UUID projectId);
}
