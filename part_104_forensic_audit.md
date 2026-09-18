# Part 104 Forensic Audit

## 1. Authoritative Part/Title
PART 104 — SHA-256 ARTIFACT INTEGRITY

## 2. Master Location
Line 43546

## 3. Extension Trace
- 6.28 Data Integrity
- 14.20 Measurement Integrity
- "Integrity failures must not silently become established knowledge"

## 4. Numbering Resolution
See `part_104_numbering_resolution.md`.

## 5. Requirements Matrix
| Requirement | Extension Section | Existing Implementation | Required Change | Tests |
| --- | --- | --- | --- | --- |
| SHA-256 canonical hashing | 6.28 | HashingAndCountingInputStream | Exists, reuse exactly | testVerifyPhysicalFile_Success |
| Integrity verification | 14.20 | Missing verification logic in FileService | Add verifyPhysicalFile and endpoint | testVerifyPhysicalFile_Mismatch |
| Security/Isolation | N/A | FileService.requireAccess | Applied in verification logic | FileServiceTest |

## 6. Existing Hashing Architecture
Reused `HashingAndCountingInputStream` in `com.gsir.files.storage`. Streaming natively supported without unbounded memory. No duplicate architectures were introduced.

## 7. Canonical SHA-256 Implementation
`HashingAndCountingInputStream` processes the stream natively calculating exact byte sizes and deterministic 256-bit hashes.

## 8. Artifact/Version Integration
FileService automatically registers `checksum` into `FileVersionEntity` and `StorageObjectEntity`.

## 9. Storage Integration
Object keys are formatted securely and retrieved via `ObjectStore`. Streaming buffers properly read back content.

## 10. Research-Artifact Integration
`ArtifactAssuranceService` already correctly binds `expectedHash`. Integrated safely.

## 11. Provenance Integration
`ExecutionProvenanceService` and `DocumentProcessingService` both pass the fileId/versionId forward protecting provenance bounds.

## 12. Verification Behavior
Added `verifyPhysicalFile` to `FileService` which streams the actual object, re-computes hash and reliably returns VERIFIED, MISMATCH, UNAVAILABLE, or INVALID INPUT.

## 13. Tamper Detection
Tested that changing bytes locally leads directly to MISMATCH without erasing expected Hash. 

## 14. Immutability
`FileVersionEntity` and `StorageObjectEntity` are functionally append-only after transaction commits. No API provided for arbitrary hash mutations.

## 15. Version Semantics
Verified. Modifying a file correctly spawns a new version via `uploadPhysicalFile`.

## 16-23. Verification details
- Concurrency, idempotency natively supported by FileService.
- No DB migrations required.
- New API: POST `/api/v1/files/{fileId}/versions/{versionId}/verify`.

## 24. Real application-flow verification
Tested the logic in Service unit tests matching standard Spring MVC flow.

## 25. Tests
Success. 

## 26. Package Result
BUILD SUCCESS. Total time: 03:10 min.

## 27. Regression Result
SUCCESS (290 tests passed, 0 failures, 0 errors, 0 skipped). No security or integrity regressions.

## 28. Whole-GSIR Gap Matrix
- Registration/Login: PARTIAL
- 2FA/Password recovery: MISSING
- Billing/Invoices: MISSING

## 29. Unresolved Limitations
None

## 30. Deferred Work
Frontend integrity badge rendering deferred.
