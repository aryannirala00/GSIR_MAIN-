package com.gsir.ingestion.service;

import com.gsir.ingestion.model.SourceRegistration;
import com.gsir.ingestion.model.SourceType;

import java.util.Optional;
import java.util.UUID;

public interface SourceRegistryService {
    SourceRegistration registerSource(String provider, String url, SourceType type, UUID projectId);
    Optional<SourceRegistration> getSource(UUID sourceId, UUID projectId);
    void quarantineSource(UUID sourceId, UUID projectId, String reason);
}
