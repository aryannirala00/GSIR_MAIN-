package com.gsir.provenance.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ProvAgent(
    UUID id,
    UUID projectId,
    AgentType agentType,
    String name,
    Map<String, Object> metadata,
    Instant createdAt
) {}
