package com.gsir.ingestion.service;

import com.gsir.ingestion.model.SourceRegistration;
import com.gsir.ingestion.model.SourceType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class DefaultSourceRegistryService implements SourceRegistryService {

    @Override
    public SourceRegistration registerSource(String provider, String url, SourceType type, UUID projectId) {
        return new SourceRegistration(
            UUID.randomUUID(),
            type,
            provider,
            url,
            "DIRECT_DOWNLOAD",
            "UNKNOWN",
            "REGISTERED",
            projectId,
            Instant.now()
        );
    }

    @Override
    public Optional<SourceRegistration> getSource(UUID sourceId, UUID projectId) {
        return Optional.empty();
    }

    @Override
    public void quarantineSource(UUID sourceId, UUID projectId, String reason) {
        // Implementation
    }
}
