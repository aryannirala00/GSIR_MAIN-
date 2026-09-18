package com.gsir.files;

import com.gsir.common.api.ApiResponse;
import com.gsir.common.api.PageResponse;
import com.gsir.files.dto.FileRequest;
import com.gsir.files.dto.FileResponse;
import com.gsir.files.dto.FileVersionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;
    private final com.gsir.processing.DocumentProcessingService documentProcessingService;

    public FileController(FileService fileService, com.gsir.processing.DocumentProcessingService documentProcessingService) {
        this.fileService = fileService;
        this.documentProcessingService = documentProcessingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFileMetadata(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody FileRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.uploadFileMetadata(idempotencyKey, request, userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FileResponse>>> listFiles(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UUID userId) {
        List<FileResponse> files = fileService.listFiles(projectId, userId);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(files, page, size)));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileResponse>> getFile(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.getFile(fileId, userId)));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UUID userId) {
        fileService.deleteFile(fileId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{fileId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileVersionResponse>> uploadPhysicalFile(
            @PathVariable UUID fileId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UUID userId) throws IOException {
        try (InputStream is = file.getInputStream()) {
            return ResponseEntity.ok(ApiResponse.ok(fileService.uploadPhysicalFile(fileId, is, userId)));
        }
    }

    @GetMapping("/{fileId}/versions")
    public ResponseEntity<ApiResponse<PageResponse<FileVersionResponse>>> getFileVersions(
            @PathVariable UUID fileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UUID userId) {
        List<FileVersionResponse> versions = fileService.getFileVersions(fileId, userId);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(versions, page, size)));
    }

    @GetMapping("/{fileId}/versions/{versionId}/download")
    public ResponseEntity<StreamingResponseBody> downloadPhysicalFile(
            @PathVariable UUID fileId,
            @PathVariable UUID versionId,
            @AuthenticationPrincipal UUID userId) {
        
        FileResponse file = fileService.getFile(fileId, userId);
        InputStream is = fileService.downloadPhysicalFile(fileId, versionId, userId);
        
        StreamingResponseBody responseBody = outputStream -> {
            try (is) {
                is.transferTo(outputStream);
            }
        };
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.name() + "\"")
                .contentType(file.mimeType() != null ? MediaType.parseMediaType(file.mimeType()) : MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    @PostMapping("/{fileId}/versions/{versionId}/process")
    public ResponseEntity<ApiResponse<com.gsir.jobs.JobEntity>> processDocument(
            @PathVariable UUID fileId,
            @PathVariable UUID versionId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(documentProcessingService.scheduleDocumentProcessing(fileId, versionId, idempotencyKey, userId)));
    }

    @PostMapping("/{fileId}/versions/{versionId}/verify")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> verifyPhysicalFile(
            @PathVariable UUID fileId,
            @PathVariable UUID versionId,
            @AuthenticationPrincipal UUID userId) {
        String status = fileService.verifyPhysicalFile(fileId, versionId, userId);
        return ResponseEntity.ok(ApiResponse.ok(java.util.Map.of("status", status)));
    }
}
