package com.gsir.files;

import com.gsir.common.error.GsirException;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.common.idempotency.IdempotencyRecordRepository;
import com.gsir.files.dto.FileRequest;
import com.gsir.files.dto.FileResponse;
import com.gsir.files.storage.ObjectStore;
import com.gsir.projects.ProjectAccessService;
import com.gsir.projects.ProjectEntity;
import com.gsir.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock private FileRepository fileRepository;
    @Mock private FileVersionRepository fileVersionRepository;
    @Mock private StorageObjectRepository storageObjectRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock private ObjectStore objectStore;

    private TransactionTemplate transactionTemplate;
    private FileService fileService;
    private final UuidV7Generator idGenerator = new UuidV7Generator();

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate() {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                return action.doInTransaction(null);
            }
        };

        ProjectAccessService projectAccessService = new ProjectAccessService(projectRepository);

        fileService = new FileService(
                fileRepository,
                fileVersionRepository,
                storageObjectRepository,
                projectAccessService,
                idempotencyRecordRepository,
                idGenerator,
                transactionTemplate,
                objectStore
        );
    }

    @Test
    void testUploadPhysicalFile_ConcurrentVersionCreation_ThrowsConflict() throws Exception {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        FileEntity file = new FileEntity(fileId, null, userId, "test.txt", "text/plain", 100L);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        
        when(fileVersionRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("Unique constraint"));

        GsirException ex = assertThrows(GsirException.class, () -> {
            fileService.uploadPhysicalFile(fileId, new ByteArrayInputStream("test".getBytes()), userId);
        });
        
        verify(objectStore).delete(anyString());
    }

    @Test
    void testVerifyPhysicalFile_Success() throws Exception {
        UUID fileId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        FileEntity file = new FileEntity(fileId, null, userId, "test.txt", "text/plain", 100L);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        
        // "test" string SHA-256 hash
        String expectedHash = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
        FileVersionEntity version = new FileVersionEntity(versionId, fileId, 1, expectedHash, 4L);
        when(fileVersionRepository.findById(versionId)).thenReturn(Optional.of(version));
        
        StorageObjectEntity storage = new StorageObjectEntity(UUID.randomUUID(), versionId, "LOCAL", "key", expectedHash);
        when(storageObjectRepository.findByFileVersionId(versionId)).thenReturn(java.util.List.of(storage));
        
        when(objectStore.get("key")).thenReturn(new ByteArrayInputStream("test".getBytes()));
        
        String result = fileService.verifyPhysicalFile(fileId, versionId, userId);
        org.junit.jupiter.api.Assertions.assertEquals("VERIFIED", result);
    }

    @Test
    void testVerifyPhysicalFile_Mismatch() throws Exception {
        UUID fileId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        FileEntity file = new FileEntity(fileId, null, userId, "test.txt", "text/plain", 100L);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        
        String expectedHash = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
        FileVersionEntity version = new FileVersionEntity(versionId, fileId, 1, expectedHash, 4L);
        when(fileVersionRepository.findById(versionId)).thenReturn(Optional.of(version));
        
        StorageObjectEntity storage = new StorageObjectEntity(UUID.randomUUID(), versionId, "LOCAL", "key", expectedHash);
        when(storageObjectRepository.findByFileVersionId(versionId)).thenReturn(java.util.List.of(storage));
        
        when(objectStore.get("key")).thenReturn(new ByteArrayInputStream("tampered".getBytes()));
        
        String result = fileService.verifyPhysicalFile(fileId, versionId, userId);
        org.junit.jupiter.api.Assertions.assertEquals("MISMATCH", result);
    }

    @Test
    void testVerifyPhysicalFile_Unavailable() {
        UUID fileId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        FileEntity file = new FileEntity(fileId, null, userId, "test.txt", "text/plain", 100L);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        
        FileVersionEntity version = new FileVersionEntity(versionId, fileId, 1, null, 4L);
        when(fileVersionRepository.findById(versionId)).thenReturn(Optional.of(version));
        
        String result = fileService.verifyPhysicalFile(fileId, versionId, userId);
        org.junit.jupiter.api.Assertions.assertEquals("UNAVAILABLE", result);
    }
}
