package com.gsir.ingestion.pipeline;

import com.gsir.ingestion.model.IngestionJob;
import com.gsir.ingestion.model.SourceRegistration;
import java.util.UUID;

public interface IngestionPipeline {
    IngestionJob startIngestion(SourceRegistration source, UUID projectId);
    
    void acquire(UUID jobId, UUID projectId);
    void validateIntegrity(UUID jobId, UUID projectId);
    void parseAndNormalize(UUID jobId, UUID projectId);
    void extractKnowledge(UUID jobId, UUID projectId);
    void indexContent(UUID jobId, UUID projectId);
}
