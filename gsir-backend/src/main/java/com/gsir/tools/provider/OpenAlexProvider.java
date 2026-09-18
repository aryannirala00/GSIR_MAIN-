package com.gsir.tools.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gsir.tools.network.SecureHttpClient;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OpenAlexProvider implements ResearchSourceProvider {

    private final SecureHttpClient secureHttpClient;
    private final ObjectMapper objectMapper;

    public OpenAlexProvider(SecureHttpClient secureHttpClient, ObjectMapper objectMapper) {
        this.secureHttpClient = secureHttpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderName() {
        return "OpenAlex";
    }

    @Override
    public JsonNode search(String query, int limit) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://api.openalex.org/works?search=" + encodedQuery + "&per-page=" + limit;
            
            String response = secureHttpClient.get(url);
            JsonNode root = objectMapper.readTree(response);
            
            ArrayNode results = JsonNodeFactory.instance.arrayNode();
            
            if (root.has("results")) {
                for (JsonNode item : root.get("results")) {
                    ObjectNode normalized = JsonNodeFactory.instance.objectNode();
                    normalized.put("source_id", item.path("id").asText(""));
                    normalized.put("title", item.path("title").asText(""));
                    normalized.put("publication_date", item.path("publication_date").asText(""));
                    normalized.put("provider", getProviderName());
                    results.add(normalized);
                }
            }
            
            return results;
        } catch (Exception e) {
            throw new RuntimeException("OpenAlex provider failed: " + e.getMessage(), e);
        }
    }
}
