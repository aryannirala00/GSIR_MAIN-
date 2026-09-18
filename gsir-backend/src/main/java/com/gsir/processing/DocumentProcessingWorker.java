package com.gsir.processing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.files.FileService;
import com.gsir.jobs.JobEntity;
import com.gsir.jobs.JobExecutionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class DocumentProcessingWorker {

    private final ObjectMapper objectMapper;
    private final FileService fileService;
    private final DocumentExtractionRepository extractionRepository;
    private final DocumentChunkRepository chunkRepository;
    private final UuidV7Generator idGenerator;
    private final TransactionTemplate transactionTemplate;

    public DocumentProcessingWorker(
            ObjectMapper objectMapper,
            FileService fileService,
            DocumentExtractionRepository extractionRepository,
            DocumentChunkRepository chunkRepository,
            UuidV7Generator idGenerator,
            TransactionTemplate transactionTemplate) {
        this.objectMapper = objectMapper;
        this.fileService = fileService;
        this.extractionRepository = extractionRepository;
        this.chunkRepository = chunkRepository;
        this.idGenerator = idGenerator;
        this.transactionTemplate = transactionTemplate;
    }

    @EventListener
    public void onJobExecution(JobExecutionEvent event) {
        JobEntity job = event.getJob();
        if (!"DOCUMENT_PROCESSING".equals(job.getJobType())) {
            return;
        }

        try {
            JsonNode payload = objectMapper.readTree(job.getPayload());
            UUID fileId = UUID.fromString(payload.get("fileId").asText());
            UUID versionId = UUID.fromString(payload.get("fileVersionId").asText());
            UUID userId = UUID.fromString(payload.get("userId").asText());

            processDocument(fileId, versionId, userId);
        } catch (Exception e) {
            throw new RuntimeException("Document processing failed: " + e.getMessage(), e);
        }
    }

    private void processDocument(UUID fileId, UUID versionId, UUID userId) throws Exception {
        // 1. Download bytes via FileService (streams from ObjectStore)
        // FileService verifies access internally inside downloadPhysicalFile
        InputStream is = fileService.downloadPhysicalFile(fileId, versionId, userId);

        StringBuilder textBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line).append("\n");
            }
        }
        
        String extractedText = textBuilder.toString();
        
        // 2. Chunking (naive paragraph splitting by double newline for now)
        String[] chunks = extractedText.split("\n\n+");

        // 3. Persist extraction and chunks transactionally
        transactionTemplate.executeWithoutResult(status -> {
            DocumentExtractionEntity extraction = new DocumentExtractionEntity(
                    idGenerator.generate(),
                    versionId,
                    "TEXT_PLAIN",
                    extractedText,
                    "{}" // metadata
            );
            extractionRepository.save(extraction);

            for (int i = 0; i < chunks.length; i++) {
                String chunkText = chunks[i].trim();
                if (chunkText.isEmpty()) continue;

                DocumentChunkEntity chunk = new DocumentChunkEntity(
                        idGenerator.generate(),
                        versionId,
                        i,
                        chunkText,
                        "{}"
                );
                chunkRepository.save(chunk);
            }
        });
    }
}
