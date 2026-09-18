package com.gsir.ingestion.pipeline;

import com.gsir.ingestion.model.IngestionJob;
import com.gsir.ingestion.model.IngestionState;
import com.gsir.ingestion.model.SourceRegistration;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Service
public class DefaultIngestionPipeline implements IngestionPipeline {

    @Override
    public IngestionJob startIngestion(SourceRegistration source, UUID projectId) {
        return new IngestionJob(
            UUID.randomUUID(),
            source.sourceId(),
            "v1",
            IngestionState.QUEUED,
            Instant.now(),
            null,
            "worker-1",
            "1.0",
            Collections.emptyList(),
            Collections.emptyList(),
            projectId
        );
    }

    @Override
    public void acquire(UUID jobId, UUID projectId) { }

    @Override
    public void validateIntegrity(UUID jobId, UUID projectId) { }

    @Override
    public void parseAndNormalize(UUID jobId, UUID projectId) { }

    @Override
    public void extractKnowledge(UUID jobId, UUID projectId) { }

    @Override
    public void indexContent(UUID jobId, UUID projectId) { }
}
