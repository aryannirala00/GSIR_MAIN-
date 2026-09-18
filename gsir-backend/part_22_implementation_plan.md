# VOLUME III PART 22 IMPLEMENTATION PLAN
Target: Files, Document Ingestion (Logical Metadata Tracking Phase)

## 1. Executive Summary
This implementation plan establishes the architectural contract for the **Logical Metadata Tracking Phase** of Volume III Part 22. It focuses strictly on `FileEntity`, `FileVersionEntity`, and `StorageObjectEntity` tracking, isolating logical persistence from deferred actual binary storage and deferred document extraction workflows.

## 2. Component Classification

| Requirement | Evidence | Status |
|---|---|---|
| `files` table | `V1__initial_schema.sql` | IMPLEMENTED (SQL) |
| `file_versions` table | `V1__initial_schema.sql` | IMPLEMENTED (SQL) |
| `storage_objects` table | `V1__initial_schema.sql` | IMPLEMENTED (SQL) |
| File Entities (`FileEntity`, etc) | Missing from `src/main/java` | MISSING |
| File Repositories | Missing from `src/main/java` | MISSING |
| File API Controller | Missing from `src/main/java` | MISSING |
| File Authorization | `ProjectAccessService` exists | PARTIAL (To be integrated) |
| Attachment Links | `NoteAttachmentEntity` exists | IMPLEMENTED |

## 3. Dependency Analysis
- **Organization, Workspace, Project, User**: IMPLEMENTED (`com.gsir.projects.*`, `com.gsir.workspaces.*`)
- **ProjectAccessService**: IMPLEMENTED (`ProjectAccessService.java`). Will be used to authorize File access.
- **Idempotency Engine**: IMPLEMENTED (`com.gsir.common.idempotency.*`). Ready for use in `FileService`.
- **Notes System**: IMPLEMENTED (`NoteAttachmentEntity`). Ready to link to `FileEntity`.

## 4. Minimum Implementation Scope

### MUST IMPLEMENT (Phase 1: Logical Metadata Tracking)
- `FileEntity`, `FileVersionEntity`, `StorageObjectEntity` mappings to V1 SQL tables.
- `FileService` for creating, tracking, and updating file states (`status = 'ACTIVE'`, etc.).
- Secure Tenancy check (Files belong to Projects or Users).
- `FileController` exposing REST metadata endpoints.
- Optimistic locking or Unique Constraints for version race protection (`UNIQUE (file_id, version_number)`).

### DEFERRED (Future Phases)
- Binary streaming to S3/Disk (Simulated in this phase via metadata records).
- Document Extraction & Chunking (`document_extractions`, `document_chunks`).
- AI Analysis (`POST /api/v1/documents/*/ai/*`).
- OCR & Vector Databases.

## 5. Database Analysis
**No database changes are required.** The existing V1 migrations perfectly define the required tables:
- `files`
- `file_versions`
- `storage_objects`

## 6. API Contract

| Method | Path | Request DTO | Response DTO | Auth |
|---|---|---|---|---|
| `POST` | `/api/v1/files/upload` | `FileUploadRequest` | `FileResponse` | `userId` Attribute + ProjectAccess |
| `GET` | `/api/v1/files` | Query Params | `List<FileResponse>` | `userId` Attribute + ProjectAccess |
| `GET` | `/api/v1/files/{file_id}` | None | `FileResponse` | `userId` Attribute + ProjectAccess |
| `PATCH` | `/api/v1/files/{file_id}` | `FileUpdateRequest` | `FileResponse` | `userId` Attribute + ProjectAccess |
| `DELETE`| `/api/v1/files/{file_id}` | None | `FileResponse` | `userId` Attribute + ProjectAccess |
| `GET` | `/api/v1/files/{file_id}/versions`| None | `List<FileVersionResponse>`| `userId` Attribute + ProjectAccess |
| `POST` | `/api/v1/files/{file_id}/versions`| `FileVersionRequest` | `FileVersionResponse` | `userId` Attribute + ProjectAccess |

*Standard GSIR Error Codes applied (404 for unauthorized, 409 for conflicts).*

## 7. Security / Tenant Isolation
Files will strictly enforce:
- **Project Isolation**: A `file` is assigned to a `project_id` and `user_id`. `ProjectAccessService.verifyAccess(projectId, userId)` MUST pass before ANY read/write operation on the file.
- **404 Masking**: If a user requests a `file_id` belonging to a project they cannot access, the system must throw `404 RESOURCE_NOT_FOUND` to prevent existence-oracle leaks.

## 8. Document Ingestion State Machine
For the logical phase, files transition through simple states in the `status` column:
- `PENDING_UPLOAD` -> `ACTIVE`
- `ACTIVE` -> `ARCHIVED` / `DELETED`

## 9. Document AI / Extraction Boundary
This phase explicitly halts at the `file_versions` boundary. Extraction pipelines and chunks are NOT touched in this metadata phase.

## 10. Provenance
`storage_objects.provider` and `storage_objects.checksum_sha256` serve as the cryptographic provenance anchors for files. The checksum guarantees binary immutability.

## 11. Concurrency / Idempotency
- **Creation Race**: Handled via standard Idempotency-Key header on `POST /api/v1/files/upload`.
- **Version Race**: Handled organically via `CONSTRAINT file_versions_unique UNIQUE (file_id, version_number)`.

## 12. Test Design
- `FileServiceTest.java`: Complete mock-based verification of Tenant Isolation and Status transitions.
  - *Positive*: Valid upload, Valid version increment, Valid retrieval.
  - *Negative*: Cross-tenant fetch attempt, cross-tenant update attempt, missing project access.
- `FileServiceConcurrencyTest.java`: Parallel upload attempts triggering Unique Constraint / Idempotency hits.

## 13. Implementation File Plan

### CREATE
- `src/main/java/com/gsir/files/FileEntity.java`
- `src/main/java/com/gsir/files/FileVersionEntity.java`
- `src/main/java/com/gsir/files/StorageObjectEntity.java`
- `src/main/java/com/gsir/files/FileRepository.java`
- `src/main/java/com/gsir/files/FileVersionRepository.java`
- `src/main/java/com/gsir/files/StorageObjectRepository.java`
- `src/main/java/com/gsir/files/FileService.java`
- `src/main/java/com/gsir/files/FileController.java`
- `src/main/java/com/gsir/files/dto/*.java`
- `src/test/java/com/gsir/files/FileServiceTest.java`

### REUSE
- `com.gsir.projects.ProjectAccessService`
- `com.gsir.common.idempotency.*`

### DO NOT TOUCH
- `V1__initial_schema.sql` (No SQL changes allowed/needed)

## 14. Acceptance Criteria (Mapped to Logical Metadata Phase)
1. **Users can securely upload supported files.** (Mapped to `FileController.upload`)
2. **Files receive stable IDs.** (Mapped to `UuidV7Generator`)
3. **File metadata is persisted.** (Mapped to `FileEntity`)
4. **File permissions are enforced server-side.** (Mapped to `ProjectAccessService` integration)
5. **Files can be associated with Projects.** (Mapped to `FileEntity.projectId`)
6. **Files can be associated with Notes.** (Pre-mapped in Part 19 `NoteAttachmentEntity`)
7. **Files support controlled versioning.** (Mapped to `FileVersionEntity`)
8. **File APIs follow the common GSIR response architecture.** (Mapped to Global Exception Handler)

## 15. Implementation Risks
- **Security (HIGH)**: Cross-tenant IDOR if `ProjectAccessService` is bypassed.
- **Concurrency (MEDIUM)**: Race conditions on version sequence incrementing.
- **Migration (LOW)**: SQL is already perfectly structured.

## 16. Implementation Gate
**READY_FOR_IMPLEMENTATION**

