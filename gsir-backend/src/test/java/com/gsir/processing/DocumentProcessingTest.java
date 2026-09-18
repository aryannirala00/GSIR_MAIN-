package com.gsir.processing;

import com.gsir.files.FileEntity;
import com.gsir.files.FileRepository;
import com.gsir.files.FileVersionEntity;
import com.gsir.files.FileVersionRepository;
import com.gsir.files.StorageObjectEntity;
import com.gsir.files.StorageObjectRepository;
import com.gsir.files.storage.ObjectStore;
import com.gsir.jobs.JobEntity;
import com.gsir.jobs.JobRepository;
import com.gsir.users.UserEntity;
import com.gsir.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class DocumentProcessingTest {

    @Autowired private DocumentProcessingService processingService;
    @Autowired private DocumentExtractionRepository extractionRepository;
    @Autowired private DocumentChunkRepository chunkRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private FileRepository fileRepository;
    @Autowired private FileVersionRepository fileVersionRepository;
    @Autowired private StorageObjectRepository storageObjectRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private com.gsir.common.idempotency.IdempotencyRecordRepository idempotencyRecordRepository;
    @Autowired private ObjectStore objectStore;
    @Autowired private ApplicationEventPublisher eventPublisher;

    private UUID userId;
    private UUID fileId;
    private UUID versionId;

    @BeforeEach
    void setUp() throws Exception {
        cleanDb();
        UserEntity user = new UserEntity(UUID.randomUUID(), "doc@test.com", "hash", "ACTIVE");
        userRepository.save(user);
        userId = user.getId();

        fileId = UUID.randomUUID();
        FileEntity file = new FileEntity(fileId, null, userId, "doc.txt", "text/plain", 100L);
        fileRepository.save(file);

        versionId = UUID.randomUUID();
        FileVersionEntity version = new FileVersionEntity(versionId, fileId, 1, "hash", 100L);
        fileVersionRepository.save(version);

        String objectKey = "tenant/" + userId + "/file/" + fileId + "/version/" + versionId;
        StorageObjectEntity storage = new StorageObjectEntity(UUID.randomUUID(), versionId, "LOCAL", objectKey, "hash");
        storageObjectRepository.save(storage);

        String textContent = "Paragraph 1.\n\nParagraph 2.\n\nParagraph 3.";
        objectStore.put(objectKey, new ByteArrayInputStream(textContent.getBytes()));
    }

    @AfterEach
    void tearDown() {
        cleanDb();
    }

    private void cleanDb() {
        chunkRepository.deleteAll();
        extractionRepository.deleteAll();
        storageObjectRepository.deleteAll();
        fileVersionRepository.deleteAll();
        fileRepository.deleteAll();
        jobRepository.deleteAll();
        idempotencyRecordRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testDocumentProcessingPipeline() {
        // Schedule Job
        JobEntity job = processingService.scheduleDocumentProcessing(fileId, versionId, "idem-123", userId);
        assertThat(job).isNotNull();
        assertThat(job.getJobType()).isEqualTo("DOCUMENT_PROCESSING");

        // Simulate JobWorker firing the event
        eventPublisher.publishEvent(new com.gsir.jobs.JobExecutionEvent(this, job));

        // Verify extractions
        List<DocumentExtractionEntity> extractions = processingService.getExtractions(fileId, versionId, userId);
        assertThat(extractions).hasSize(1);
        assertThat(extractions.get(0).getContent()).contains("Paragraph 1");

        // Verify chunks
        List<DocumentChunkEntity> chunks = processingService.getChunks(fileId, versionId, userId);
        assertThat(chunks).hasSize(3);
        assertEquals("Paragraph 1.", chunks.get(0).getContent());
        assertEquals("Paragraph 2.", chunks.get(1).getContent());
        assertEquals("Paragraph 3.", chunks.get(2).getContent());
    }
}
