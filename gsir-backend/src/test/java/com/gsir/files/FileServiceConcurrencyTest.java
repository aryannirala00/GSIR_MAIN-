package com.gsir.files;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.common.idempotency.IdempotencyRecordRepository;
import com.gsir.files.dto.FileRequest;
import com.gsir.files.dto.FileResponse;

import com.gsir.files.dto.FileVersionResponse;
import com.gsir.organizations.OrganizationEntity;
import com.gsir.organizations.OrganizationRepository;
import com.gsir.users.UserEntity;
import com.gsir.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FileServiceConcurrencyTest {

    @Autowired private FileService fileService;
    @Autowired private FileRepository fileRepository;
    @Autowired private FileVersionRepository fileVersionRepository;
    @Autowired private StorageObjectRepository storageObjectRepository;
    @Autowired private IdempotencyRecordRepository idempotencyRecordRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;

    private final UuidV7Generator idGen = new UuidV7Generator();

    private UUID userId;

    @BeforeEach
    void setUp() {
        cleanDb();
        UserEntity user = new UserEntity(idGen.generate(), "files@test.com", "hash", "ACTIVE");
        userRepository.save(user);
        userId = user.getId();
    }

    @AfterEach
    void tearDown() {
        cleanDb();
    }

    private void cleanDb() {
        storageObjectRepository.deleteAll();
        fileVersionRepository.deleteAll();
        fileRepository.deleteAll();
        idempotencyRecordRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void testConcurrentUploadFileMetadata_Idempotency() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        String idemKey = "file-upload-" + UUID.randomUUID().toString();
        FileRequest req = new FileRequest(null, "concurrent.txt", "text/plain", 500L);

        List<FileResponse> results = new CopyOnWriteArrayList<>();
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    results.add(fileService.uploadFileMetadata(idemKey, req, userId));
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));

        // No exceptions should escape to the client (idempotency intercepts race and returns the created record)
        assertTrue(exceptions.isEmpty(), "Exceptions found: " + exceptions);

        assertEquals(threads, results.size());
        UUID firstId = results.get(0).id();
        
        for (FileResponse res : results) {
            assertEquals(firstId, res.id(), "All threads must return the exact same file ID");
        }

        assertEquals(1, fileRepository.count(), "Exactly one file entity should exist");
        assertEquals(1, idempotencyRecordRepository.count(), "Exactly one idempotency record should exist");
    }
}

