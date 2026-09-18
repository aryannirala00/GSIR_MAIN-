package com.gsir.files;

import com.gsir.common.error.CommonErrors;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.common.idempotency.IdempotencyRecordEntity;
import com.gsir.common.idempotency.IdempotencyRecordId;
import com.gsir.common.idempotency.IdempotencyRecordRepository;
import com.gsir.files.dto.FileRequest;
import com.gsir.files.dto.FileResponse;
import com.gsir.files.dto.FileVersionResponse;
import com.gsir.files.storage.HashingAndCountingInputStream;
import com.gsir.files.storage.ObjectStore;
import com.gsir.projects.ProjectAccessService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final StorageObjectRepository storageObjectRepository;
    private final ProjectAccessService projectAccessService;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final UuidV7Generator idGenerator;
    private final TransactionTemplate transactionTemplate;
    private final ObjectStore objectStore;

    public FileService(
            FileRepository fileRepository,
            FileVersionRepository fileVersionRepository,
            StorageObjectRepository storageObjectRepository,
            ProjectAccessService projectAccessService,
            IdempotencyRecordRepository idempotencyRecordRepository,
            UuidV7Generator idGenerator,
            TransactionTemplate transactionTemplate,
            ObjectStore objectStore
    ) {
        this.fileRepository = fileRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.storageObjectRepository = storageObjectRepository;
        this.projectAccessService = projectAccessService;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.idGenerator = idGenerator;
        this.transactionTemplate = transactionTemplate;
        this.objectStore = objectStore;
    }

    public FileResponse uploadFileMetadata(String idempotencyKey, FileRequest request, UUID userId) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecordId recordId = new IdempotencyRecordId(userId, idempotencyKey);
            Optional<IdempotencyRecordEntity> existingRecord = idempotencyRecordRepository.findById(recordId);
            if (existingRecord.isPresent()) {
                FileEntity file = fileRepository.findById(existingRecord.get().getResourceId())
                        .orElseThrow(() -> CommonErrors.notFound("File"));
                return FileResponse.from(file);
            }
        }

        return executeCreate(idempotencyKey, request, userId);
    }

    private FileResponse executeCreate(String idempotencyKey, FileRequest request, UUID userId) {
        try {
            return transactionTemplate.execute(status -> {
                if (request.projectId() != null) {
                    projectAccessService.requireAccess(request.projectId(), userId);
                }

                UUID fileId = idGenerator.generate();

                if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                    idempotencyRecordRepository.saveAndFlush(
                            new IdempotencyRecordEntity(new IdempotencyRecordId(userId, idempotencyKey), "File", fileId)
                    );
                }

                FileEntity file = new FileEntity(
                        fileId,
                        request.projectId(),
                        userId,
                        request.name(),
                        request.mimeType(),
                        request.sizeBytes()
                );

                file = fileRepository.save(file);
                return FileResponse.from(file);
            });
        } catch (DataIntegrityViolationException race) {
            if (idempotencyKey != null) {
                return idempotencyRecordRepository
                        .findById(new IdempotencyRecordId(userId, idempotencyKey))
                        .flatMap(rec -> fileRepository.findById(rec.getResourceId()))
                        .map(FileResponse::from)
                        .orElseThrow(() -> CommonErrors.notFound("File"));
            }
            throw CommonErrors.conflict("Concurrent conflict during file creation");
        }
    }

    @Transactional(readOnly = true)
    public FileEntity requireAccess(UUID fileId, UUID userId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> CommonErrors.notFound("File"));

        if (file.getProjectId() != null) {
            projectAccessService.requireAccess(file.getProjectId(), userId);
        } else if (!file.getUserId().equals(userId)) {
            throw CommonErrors.notFound("File");
        }

        return file;
    }

    @Transactional(readOnly = true)
    public FileResponse getFile(UUID fileId, UUID userId) {
        return FileResponse.from(requireAccess(fileId, userId));
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listFiles(UUID projectId, UUID userId) {
        if (projectId != null) {
            projectAccessService.requireAccess(projectId, userId);
            return fileRepository.findByProjectId(projectId).stream()
                    .map(FileResponse::from)
                    .toList();
        } else {
            return fileRepository.findByUserId(userId).stream()
                    .filter(f -> f.getProjectId() == null)
                    .map(FileResponse::from)
                    .toList();
        }
    }

    @Transactional
    public void deleteFile(UUID fileId, UUID userId) {
        FileEntity file = requireAccess(fileId, userId);
        file.setStatus("DELETED");
        fileRepository.save(file);
    }

    public FileVersionResponse uploadPhysicalFile(UUID fileId, InputStream inputStream, UUID userId) {
        FileEntity file = requireAccess(fileId, userId); // verify access first
        
        UUID versionId = idGenerator.generate();
        String tenantId = file.getProjectId() != null ? file.getProjectId().toString() : userId.toString();
        String objectKey = String.format("tenant/%s/file/%s/version/%s", tenantId, fileId, versionId);
        
        HashingAndCountingInputStream hcis = new HashingAndCountingInputStream(inputStream);
        
        try {
            // Stream bytes physically without holding a DB transaction
            objectStore.put(objectKey, hcis);
        } catch (IOException e) {
            throw CommonErrors.internal("Failed to store physical file bytes: " + e.getMessage());
        }

        String checksum = hcis.getChecksumSha256();
        long sizeBytes = hcis.getByteCount();

        try {
            return transactionTemplate.execute(status -> {
                // Re-fetch inside transaction to ensure we have the latest
                FileEntity f = fileRepository.findById(fileId).orElseThrow();
                
                Integer nextVersion = fileVersionRepository.findMaxVersionByFileId(fileId)
                        .map(v -> v + 1)
                        .orElse(1);

                FileVersionEntity version = new FileVersionEntity(
                        versionId,
                        fileId,
                        nextVersion,
                        checksum,
                        sizeBytes
                );
                version = fileVersionRepository.saveAndFlush(version);

                StorageObjectEntity storageObject = new StorageObjectEntity(
                        idGenerator.generate(),
                        versionId,
                        "LOCAL",
                        objectKey,
                        checksum
                );
                storageObjectRepository.save(storageObject);

                f.setSizeBytes(sizeBytes);
                fileRepository.save(f);

                return FileVersionResponse.from(version);
            });
        } catch (Exception e) {
            // DB Transaction failed (e.g. unique constraint). Cleanup physical object safely.
            try {
                objectStore.delete(objectKey);
            } catch (IOException ioException) {
                // Log and ignore to prevent masking the original exception
            }
            throw CommonErrors.conflict("Failed to persist version metadata: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<FileVersionResponse> getFileVersions(UUID fileId, UUID userId) {
        requireAccess(fileId, userId);
        return fileVersionRepository.findByFileId(fileId).stream()
                .map(FileVersionResponse::from)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public InputStream downloadPhysicalFile(UUID fileId, UUID versionId, UUID userId) {
        requireAccess(fileId, userId);
        
        FileVersionEntity version = fileVersionRepository.findById(versionId)
                .orElseThrow(() -> CommonErrors.notFound("File version"));
                
        if (!version.getFileId().equals(fileId)) {
            throw CommonErrors.notFound("File version does not belong to this file");
        }
        
        List<StorageObjectEntity> storageObjects = storageObjectRepository.findByFileVersionId(versionId);
        if (storageObjects.isEmpty()) {
            throw CommonErrors.internal("Physical storage object missing for version");
        }
        StorageObjectEntity storage = storageObjects.get(0);
                
        try {
            return objectStore.get(storage.getObjectKey());
        } catch (IOException e) {
            throw CommonErrors.internal("Failed to retrieve physical file");
        }
    }


    @Transactional(readOnly = true)
    public String verifyPhysicalFile(UUID fileId, UUID versionId, UUID userId) {
        requireAccess(fileId, userId);
        
        FileVersionEntity version = fileVersionRepository.findById(versionId)
                .orElseThrow(() -> CommonErrors.notFound("File version"));
                
        if (!version.getFileId().equals(fileId)) {
            return "INVALID INPUT";
        }
        
        if (version.getChecksumSha256() == null || version.getChecksumSha256().isBlank()) {
            return "UNAVAILABLE";
        }
        
        List<StorageObjectEntity> storageObjects = storageObjectRepository.findByFileVersionId(versionId);
        if (storageObjects.isEmpty()) {
            return "INVALID INPUT";
        }
        StorageObjectEntity storage = storageObjects.get(0);
                
        try (InputStream is = objectStore.get(storage.getObjectKey())) {
            HashingAndCountingInputStream hcis = new HashingAndCountingInputStream(is);
            byte[] buffer = new byte[8192];
            while (hcis.read(buffer) != -1) {
                // stream through
            }
            String actualHash = hcis.getChecksumSha256();
            if (actualHash.equalsIgnoreCase(version.getChecksumSha256())) {
                return "VERIFIED";
            } else {
                return "MISMATCH";
            }
        } catch (IOException e) {
            return "INVALID INPUT";
        }
    }
}
