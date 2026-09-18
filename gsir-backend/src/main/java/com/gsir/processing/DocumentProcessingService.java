package com.gsir.processing;

import com.gsir.common.error.CommonErrors;
import com.gsir.files.FileService;
import com.gsir.files.FileVersionEntity;
import com.gsir.files.FileVersionRepository;
import com.gsir.jobs.JobEntity;
import com.gsir.jobs.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
public class DocumentProcessingService {

    private final JobService jobService;
    private final FileService fileService;
    private final FileVersionRepository fileVersionRepository;
    private final DocumentExtractionRepository extractionRepository;
    private final DocumentChunkRepository chunkRepository;

    public DocumentProcessingService(
            JobService jobService,
            FileService fileService,
            FileVersionRepository fileVersionRepository,
            DocumentExtractionRepository extractionRepository,
            DocumentChunkRepository chunkRepository) {
        this.jobService = jobService;
        this.fileService = fileService;
        this.fileVersionRepository = fileVersionRepository;
        this.extractionRepository = extractionRepository;
        this.chunkRepository = chunkRepository;
    }

    public JobEntity scheduleDocumentProcessing(UUID fileId, UUID versionId, String idempotencyKey, UUID userId) {
        // Enforce authorization bounds via FileService
        fileService.requireAccess(fileId, userId);
        
        // Ensure version exists and belongs to the file
        FileVersionEntity version = fileVersionRepository.findById(versionId)
                .orElseThrow(() -> CommonErrors.notFound("File version"));
                
        if (!version.getFileId().equals(fileId)) {
            throw CommonErrors.notFound("File version does not belong to this file");
        }

        String payload = String.format("{\"fileId\":\"%s\",\"fileVersionId\":\"%s\",\"userId\":\"%s\"}", fileId, versionId, userId);
        
        // Safely schedule the job with native idempotency support
        return jobService.submitJob(userId, idempotencyKey, "DOCUMENT_PROCESSING", payload);
    }
    
    @Transactional(readOnly = true)
    public List<DocumentExtractionEntity> getExtractions(UUID fileId, UUID versionId, UUID userId) {
        fileService.requireAccess(fileId, userId);
        return extractionRepository.findByFileVersionId(versionId);
    }

    @Transactional(readOnly = true)
    public List<DocumentChunkEntity> getChunks(UUID fileId, UUID versionId, UUID userId) {
        fileService.requireAccess(fileId, userId);
        return chunkRepository.findByFileVersionIdOrderByChunkIndexAsc(versionId);
    }
}
