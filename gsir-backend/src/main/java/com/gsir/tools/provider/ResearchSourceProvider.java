package com.gsir.tools.provider;

import com.fasterxml.jackson.databind.JsonNode;

public interface ResearchSourceProvider {
    String getProviderName();
    JsonNode search(String query, int limit);
}
