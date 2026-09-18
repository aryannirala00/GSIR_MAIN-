# PART 334 — ACTUAL REPOSITORY AUDIT AND NEXT-CAPABILITY SELECTION

## 1. Executive Conclusion

The GSIR backend repository contains a well-structured Spring Boot 3.4.3 / Java 21 application with a comprehensive 36-table PostgreSQL schema, 79 production Java source files, 16 test classes (78 tests), and 4 Flyway migrations. The implementation covers foundational domain models (Users, Organizations, Workspaces, Projects), domain features (Notes, Files metadata, Chat/Conversations), cross-cutting concerns (Idempotency, UUIDv7, Scientific data types, Numeric safety, Error handling, Request ID tracing), and verified concurrency safety.

**CRITICAL FINDING**: The repository has **NO authentication or authorization infrastructure**. No JWT, no OAuth, no SecurityFilterChain, no authentication filter, no session validation, no password hashing service. All controllers use `@RequestAttribute("userId")` which is **never populated by any filter or middleware**. The database schema defines `users`, `sessions`, `refresh_tokens`, and `password_hash` columns, but no Java code implements registration, login, password verification, token issuance, or session management.

**SELECTED NEXT CAPABILITY**: Authentication & Security Foundation (Registration, Login, Session/JWT, SecurityFilterChain)

---

## 2. Repository Identity

| Property | Value |
|---|---|
| Repository root | `C:\AI START UP\gsir-backend` |
| Git | NOT a git repository (no `.git` directory found in `gsir-backend` or `C:\AI START UP`) |
| Branch | N/A |
| Current commit | N/A |
| Working tree | N/A (no git) |
| Backend root | `C:\AI START UP\gsir-backend\src\main\java\com\gsir` |
| Frontend root | **NONE FOUND** — no `package.json`, no React/Angular/Vue project under `C:\AI START UP` |
| Database/migration root | `C:\AI START UP\gsir-backend\src\main\resources\db\migration` |
| Test root | `C:\AI START UP\gsir-backend\src\test\java\com\gsir` |
| Configuration root | `C:\AI START UP\gsir-backend\src\main\resources` |

---

## 3. Git/Worktree State

| Check | Result |
|---|---|
| Git initialized | **NO** — `fatal: not a git repository` in both `gsir-backend` and `C:\AI START UP` |
| Branch tracking | **NOT AVAILABLE** |
| Uncommitted changes | **CANNOT DETERMINE** |
| History available | **NO** |

> [!CAUTION]
> The absence of git version control is a significant operational risk. No rollback capability, no change history, no diff auditing.

---

## 4. Complete Implementation Inventory

### 4.1 Production Source Files (79 files)

**Package: `com.gsir` (1 file)**
- `GsirApplication.java` — Spring Boot entry point

**Package: `com.gsir.common.error` (4 files)**
- `ApiError.java` — Standardized API error response record
- `CommonErrors.java` — Factory for common error types (notFound, forbidden, unauthorized, conflict, invalid)
- `GlobalExceptionHandler.java` — `@RestControllerAdvice` handling GsirException, validation, and unexpected errors
- `GsirException.java` — Base exception with HTTP status and error code

**Package: `com.gsir.common.id` (2 files)**
- `GsirIdGenerator.java` — Interface for UUIDv7 generation
- `UuidV7Generator.java` — RFC 9562 UUIDv7 implementation with monotonic ordering

**Package: `com.gsir.common.idempotency` (3 files)**
- `IdempotencyRecordEntity.java` — JPA entity for idempotency tracking
- `IdempotencyRecordId.java` — Composite key (userId, idempotencyKey)
- `IdempotencyRecordRepository.java` — JPA repository

**Package: `com.gsir.common.math` (2 files)**
- `NumericSafety.java` — Safe division, sqrt, log with NaN/Infinity protection
- `Statistics.java` — Mean, sample variance, standard deviation with validation

**Package: `com.gsir.common.persistence` (2 files)**
- `AuditedEntity.java` — Abstract base entity with @PrePersist/@PreUpdate timestamps
- `IdGenerator.java` — ID generation interface

**Package: `com.gsir.common.science` (1 file)**
- `EvidenceStatus.java` — Scientific evidence classification enum (8 values)

**Package: `com.gsir.common.web` (1 file)**
- `RequestIdFilter.java` — Servlet filter for X-Request-ID propagation via MDC

**Package: `com.gsir.conversations` (16 files)**
- `ConversationController.java` — 9 REST endpoints
- `ConversationEntity.java` — JPA entity (12 fields including V4 additions)
- `ConversationRepository.java` — JPA repository with tenant-isolated queries
- `ConversationService.java` — Business logic with idempotency, IDOR protection
- `MessageEntity.java` — JPA entity (9 fields including V4 additions)
- `MessagePartEntity.java` — JPA entity (6 fields)
- `MessagePartRepository.java` — JPA repository
- `MessageRepository.java` — JPA repository
- DTOs: `ConversationResponse`, `CreateConversationRequest`, `CreateMessageRequest`, `MessagePartRequest`, `MessagePartResponse`, `MessageResponse`, `UpdateConversationRequest`, `UpdateMessageRequest`

**Package: `com.gsir.files` (12 files)**
- `FileController.java` — 6 REST endpoints
- `FileEntity.java`, `FileVersionEntity.java`, `StorageObjectEntity.java` — JPA entities
- `FileRepository.java`, `FileVersionRepository.java`, `StorageObjectRepository.java` — Repositories
- `FileService.java` — Business logic with idempotency
- DTOs: `FileRequest`, `FileResponse`, `FileVersionRequest`, `FileVersionResponse`

**Package: `com.gsir.notes` (15 files)**
- `NoteController.java` — 14 REST endpoints
- `NoteEntity.java`, `NoteVersionEntity.java`, `NoteRelationshipEntity.java`, `NoteAttachmentEntity.java`, `KnowledgeCandidateEntity.java` — JPA entities
- `NoteRepository.java`, `NoteVersionRepository.java`, `NoteRelationshipRepository.java`, `NoteAttachmentRepository.java`, `KnowledgeCandidateRepository.java` — Repositories
- `NoteService.java`, `NoteAiService.java`, `NoteKnowledgeService.java` — Services
- `NoteRequest.java`, `NoteResponse.java` — DTOs

**Package: `com.gsir.organizations` (5 files)**
- `OrganizationEntity.java`, `OrganizationMembershipEntity.java` — JPA entities
- `OrganizationMembershipId.java` — Composite key
- `OrganizationMembershipRepository.java`, `OrganizationRepository.java` — Repositories

**Package: `com.gsir.outbox` (2 files)**
- `OutboxEvent.java` — JPA entity for transactional outbox pattern
- `OutboxEventRepository.java` — Repository

**Package: `com.gsir.projects` (5 files)**
- `CreateProjectCommand.java` — Command record
- `ProjectAccessService.java` — Multi-tenant authorization with IDOR prevention
- `ProjectCommandService.java` — Project creation with idempotency + outbox
- `ProjectEntity.java`, `ProjectRepository.java` — Entity + repository

**Package: `com.gsir.users` (2 files)**
- `UserEntity.java` — JPA entity (id, email, passwordHash, status, timestamps)
- `UserRepository.java` — JPA repository

**Package: `com.gsir.workspaces` (5 files)**
- `WorkspaceEntity.java`, `WorkspaceMembershipEntity.java` — JPA entities
- `WorkspaceMembershipId.java` — Composite key
- `WorkspaceMembershipRepository.java`, `WorkspaceRepository.java` — Repositories

### 4.2 Test Files (16 files, 78 tests)

| Test Class | Tests | Type | Concurrency |
|---|:---:|---|:---:|
| `GsirApplicationTests` | 1 | Integration | No |
| `DatabaseConfigurationTest` | 2 | Integration | No |
| `UuidV7GeneratorTest` | 6 | Unit | No |
| `NumericSafetyTest` | 13 | Unit | No |
| `StatisticsTest` | 14 | Unit | No |
| `AuditedEntityTest` | 2 | Unit | No |
| `EvidenceStatusTest` | 1 | Unit | No |
| `GlobalExceptionHandlerTest` | 7 | WebMvcTest | No |
| `ConversationServiceTest` | 3 | Unit/Mockito | No |
| `ConversationServiceConcurrencyTest` | 1 | Integration | **YES** |
| `FileServiceTest` | 5 | Unit/Mockito | No |
| `FileServiceConcurrencyTest` | 1 | Integration | **YES** |
| `NoteServiceTest` | 5 | Unit/Mockito | No |
| `ProjectAccessServiceTest` | 8 | Integration | No |
| `ProjectCommandServiceTest` | 8 | Integration | No |
| `ProjectCommandServiceConcurrencyTest` | 1 | Integration | **YES** |

---

## 5. Database/Schema Inventory

### 5.1 Migrations

| Migration | Tables Created | Columns Added | Purpose |
|---|:---:|:---:|---|
| V1__initial_schema.sql | 30 | — | Core schema (users through audit_events) |
| V2__idempotency.sql | 1 | — | Idempotency records |
| V3__notes_architecture.sql | 5 | 7 (ALTER on notes) | Notes versioning, tags, relationships, attachments, knowledge candidates |
| V4__part_21_chat_schema_alignment.sql | 0 | 6 (ALTER on conversations, messages) | Chat Part 21 alignment (model_id, archived_at, metadata, updated_at) |

### 5.2 Total Tables: **36**

| # | Table | Migration | Entity Exists | Repository Exists | Service Exists |
|---|---|:---:|:---:|:---:|:---:|
| 1 | users | V1 | ✅ | ✅ | ❌ |
| 2 | profiles | V1 | ❌ | ❌ | ❌ |
| 3 | sessions | V1 | ❌ | ❌ | ❌ |
| 4 | refresh_tokens | V1 | ❌ | ❌ | ❌ |
| 5 | organizations | V1 | ✅ | ✅ | ❌ |
| 6 | organization_memberships | V1 | ✅ | ✅ | ❌ |
| 7 | workspaces | V1 | ✅ | ✅ | ❌ |
| 8 | workspace_memberships | V1 | ✅ | ✅ | ❌ |
| 9 | projects | V1 | ✅ | ✅ | ✅ |
| 10 | notes | V1+V3 | ✅ | ✅ | ✅ |
| 11 | web_links | V1 | ❌ | ❌ | ❌ |
| 12 | conversations | V1+V4 | ✅ | ✅ | ✅ |
| 13 | messages | V1+V4 | ✅ | ✅ | ✅ (via ConversationService) |
| 14 | message_parts | V1 | ✅ | ✅ | ✅ (via ConversationService) |
| 15 | files | V1 | ✅ | ✅ | ✅ |
| 16 | file_versions | V1 | ✅ | ✅ | ✅ (via FileService) |
| 17 | storage_objects | V1 | ✅ | ✅ | ✅ (via FileService) |
| 18 | document_extractions | V1 | ❌ | ❌ | ❌ |
| 19 | document_chunks | V1 | ❌ | ❌ | ❌ |
| 20 | ai_providers | V1 | ❌ | ❌ | ❌ |
| 21 | ai_models | V1 | ❌ | ❌ | ❌ |
| 22 | ai_model_versions | V1 | ❌ | ❌ | ❌ |
| 23 | prompt_templates | V1 | ❌ | ❌ | ❌ |
| 24 | prompt_versions | V1 | ❌ | ❌ | ❌ |
| 25 | ai_runs | V1 | ❌ | ❌ | ❌ |
| 26 | ai_outputs | V1 | ❌ | ❌ | ❌ |
| 27 | jobs | V1 | ❌ | ❌ | ❌ |
| 28 | job_attempts | V1 | ❌ | ❌ | ❌ |
| 29 | outbox_events | V1 | ✅ | ✅ | PARTIAL (creation only) |
| 30 | audit_events | V1 | ❌ | ❌ | ❌ |
| 31 | idempotency_records | V2 | ✅ | ✅ | ✅ (reused by services) |
| 32 | note_versions | V3 | ✅ | ✅ | ✅ (via NoteService) |
| 33 | note_tags | V3 | ❌ | ❌ | ❌ |
| 34 | note_relationships | V3 | ✅ | ✅ | ✅ (via NoteService) |
| 35 | note_attachments | V3 | ✅ | ✅ | ✅ (via NoteService) |
| 36 | knowledge_candidates | V3 | ✅ | ✅ | ✅ (via NoteKnowledgeService) |

**Tables with full Entity+Repository+Service**: 14/36  
**Tables with Entity+Repository only**: 7/36 (org, workspace, membership models used for queries)  
**Tables schema-only (no Java code)**: 15/36

---

## 6. Part 14–22 Implementation Verification

| Part | Topic | Claimed | Evidence | Actual Status | Limitations |
|---|---|---|---|---|---|
| Part 14 | Idempotency | COMPLETE | `IdempotencyRecordEntity`, `TransactionTemplate`, concurrency tests (3 classes), V2 migration | **IMPLEMENTED** | Only applied to Projects, Files, Conversations |
| Part 15 | Scientific Data Types | COMPLETE | `EvidenceStatus.java` (8 values), `EvidenceStatusTest` (1 test) | **IMPLEMENTED** | Enum only; not yet integrated with domain entities |
| Part 16 | Mathematics Engine | COMPLETE | `Statistics.java`, `NumericSafety.java`, 27 tests | **IMPLEMENTED** | Foundation only; no advanced scientific computation |
| Part 17 | Numerical Safety | COMPLETE | Part of `NumericSafety.java` (safeDivide, safeSqrt, safeLog) | **IMPLEMENTED** | Merged with Part 16 |
| Part 18 | Numerical Safety (cont.) | COMPLETE | Same as Part 17 | **IMPLEMENTED** | Merged with Part 16/17 |
| V3 Part 19 | Notes Architecture | COMPLETE | 15 files, V3 migration, 5 tests, 14 endpoints | **IMPLEMENTED** | AI integration (NoteAiService) is a stub |
| V3 Part 21 | Chat Architecture (Phase 1) | COMPLETE | 16 files, V4 migration, 4 tests, 9 endpoints | **IMPLEMENTED** | Phase 1 only (11/34 criteria) |
| Part 22 | Files (Logical Metadata) | COMPLETE | 12 files, 6 tests, 6 endpoints | **IMPLEMENTED** | Metadata tracking only; no physical storage, no document processing |

---

## 7. Security Audit

> [!CAUTION]
> **CRITICAL SECURITY GAP — NO AUTHENTICATION INFRASTRUCTURE EXISTS**

| Security Component | Status | Evidence |
|---|---|---|
| Registration endpoint | **MISSING** | No registration controller, service, or DTO found |
| Login endpoint | **MISSING** | No login controller, service, or DTO found |
| Password hashing | **MISSING** | `UserEntity.passwordHash` field exists but no `PasswordEncoder`, no BCrypt/Argon2 |
| Session management | **MISSING** | `sessions` table in V1 but no `SessionEntity`, no session service |
| Refresh tokens | **MISSING** | `refresh_tokens` table in V1 but no entity, no rotation logic |
| JWT issuance | **MISSING** | No JWT dependency in pom.xml, no JWT classes found |
| JWT verification | **MISSING** | No JWT filter, no token validation |
| SecurityFilterChain | **MISSING** | No Spring Security dependency, no security configuration |
| Spring Security dependency | **MISSING** | Not in pom.xml |
| Authentication filter | **MISSING** | No filter populates `@RequestAttribute("userId")` |
| SecurityContext | **MISSING** | No SecurityContext usage |
| CORS configuration | **MISSING** | No CORS configuration found |
| CSRF protection | **MISSING** | No CSRF configuration (but stateless API, so acceptable if JWT) |
| Rate limiting | **MISSING** | No rate limiting implementation |
| Audit event service | **MISSING** | `audit_events` table exists but no service writes to it |

**How userId reaches controllers currently**: It does NOT. `@RequestAttribute("userId")` is used but no middleware, filter, or interceptor sets this attribute. In production, every endpoint would throw a `MissingServletRequestParameterException` or receive `null`.

---

## 8. Tenant/IDOR Audit

| Check | Status | Evidence |
|---|---|---|
| `ProjectAccessService` | **IMPLEMENTED** | 2 overloaded `requireAccess` methods with full hierarchy validation |
| Project IDOR protection | **PASS** | `findAccessibleProject` joins through org→workspace→project memberships |
| Note tenant isolation | **PARTIAL** | Notes checked via userId or projectAccessService |
| File tenant isolation | **PARTIAL** | Files checked via userId or projectAccessService |
| Conversation tenant isolation | **PASS** | `getAccessibleConversation` checks userId or project access |
| Message tenant isolation | **PASS** | Messages verified through conversation ownership |
| Cross-organization rejection | **PASS** | Verified by `ProjectAccessServiceTest` |
| Cross-workspace rejection | **PASS** | Verified by `ProjectAccessServiceTest` |
| 404 not 403 for unauthorized | **PASS** | `CommonErrors.notFound()` used consistently |

> [!WARNING]
> IDOR protections exist at the service layer but are **architecturally dependent on the caller providing a valid, authenticated userId**. Without authentication, these protections are **bypass-able** since any client could supply any userId.

---

## 9. API Audit

### 9.1 Implemented Endpoints

**ConversationController (9 endpoints)**
| # | Method | Path |
|---|---|---|
| 1 | POST | `/api/v1/conversations` |
| 2 | GET | `/api/v1/conversations` |
| 3 | GET | `/api/v1/conversations/{id}` |
| 4 | PATCH | `/api/v1/conversations/{id}` |
| 5 | DELETE | `/api/v1/conversations/{id}` |
| 6 | POST | `/api/v1/conversations/{id}/messages` |
| 7 | GET | `/api/v1/conversations/{id}/messages` |
| 8 | PATCH | `/api/v1/messages/{id}` |
| 9 | DELETE | `/api/v1/messages/{id}` |

**NoteController (14 endpoints)** — CRUD + versions, relationships, attachments, knowledge candidates, AI summary

**FileController (6 endpoints)** — File metadata CRUD + versions

**ProjectCommandService** — Creates projects (called indirectly via integration tests; no dedicated ProjectController found in main source)

> [!NOTE]
> No `ProjectController` REST controller class was found. Project creation appears to happen only through integration tests that call `ProjectCommandService` directly.

### 9.2 Missing from Master
- No user registration/login endpoints
- No organization management endpoints  
- No workspace management endpoints
- No web_links endpoints
- No AI Gateway endpoints
- No search endpoints
- No research endpoints
- No job management endpoints

---

## 10. AI Gateway Audit

| Component | Status | Evidence |
|---|---|---|
| Database tables | **SCHEMA ONLY** | `ai_providers`, `ai_models`, `ai_model_versions`, `prompt_templates`, `prompt_versions`, `ai_runs`, `ai_outputs` (7 tables in V1) |
| Java entities | **MISSING** | No entity classes for any AI table |
| Java repositories | **MISSING** | No repository classes |
| Java services | **MISSING** | No AI service classes |
| Provider adapters | **MISSING** | No provider integration code |
| Model routing | **MISSING** | No routing logic |
| Streaming | **MISSING** | No SSE/WebSocket support |

**VERDICT: AI Gateway is DATABASE-ONLY. No functional capability exists.**

---

## 11. Cognitive Runtime Audit

| Component | Status |
|---|---|
| Perception | MISSING |
| Attention | MISSING |
| Cognitive state | MISSING |
| Working memory | MISSING |
| Reasoning | MISSING |
| Planning | MISSING |
| Action/tool control | MISSING |
| Learning | MISSING |
| Metacognition | MISSING |

**VERDICT: No Cognitive Runtime exists. Not specified, not scaffolded, not implemented.**

---

## 12. Research Intelligence Audit

**VERDICT: MISSING. No research-related code exists beyond the schema tables.**

---

## 13. Files/Document Audit

| Capability | Status |
|---|---|
| File metadata (CRUD) | **IMPLEMENTED** |
| File versions (metadata) | **IMPLEMENTED** |
| Storage objects (metadata) | **IMPLEMENTED** |
| Physical file storage | **MISSING** (no S3/MinIO/local storage integration) |
| Document ingestion pipeline | **MISSING** |
| PDF parsing / OCR | **MISSING** |
| Text extraction | **MISSING** |
| Chunking | **MISSING** |
| Document extractions table | **SCHEMA ONLY** |
| Document chunks table | **SCHEMA ONLY** |

---

## 14. Notes Audit

| Capability | Status |
|---|---|
| Note CRUD | **IMPLEMENTED** |
| Note versions | **IMPLEMENTED** |
| Note relationships | **IMPLEMENTED** |
| Note attachments | **IMPLEMENTED** |
| Knowledge candidates | **IMPLEMENTED** |
| NoteAiService | **STUB** (exists but likely placeholder) |
| NoteKnowledgeService | **IMPLEMENTED** |
| Note tags | **SCHEMA ONLY** (V3 table exists, no entity) |

---

## 15. Chat Audit

| Capability | Status |
|---|---|
| Conversation CRUD | **IMPLEMENTED** |
| Message creation | **IMPLEMENTED** |
| Message parts | **IMPLEMENTED** |
| Message ordering | **IMPLEMENTED** (createdAt ASC) |
| Message branching | **IMPLEMENTED** (parent_message_id) |
| Message editing | **IMPLEMENTED** (PATCH status) |
| Message soft delete | **IMPLEMENTED** |
| Conversation lifecycle | **IMPLEMENTED** (ACTIVE/ARCHIVED/DELETED) |
| Idempotency | **IMPLEMENTED** (conversation creation) |
| Concurrency test | **PASS** (10-thread CountDownLatch) |
| AI integration | **MISSING** (Phase 2+) |

---

## 16. Jobs/Queue/Outbox Audit

| Component | Status | Evidence |
|---|---|---|
| `jobs` table | **SCHEMA ONLY** | No entity, no service, no worker |
| `job_attempts` table | **SCHEMA ONLY** | No entity |
| `outbox_events` table | **PARTIAL** | Entity exists, events created during project creation. No publisher, no consumer, no delivery mechanism |
| Event delivery | **MISSING** | No scheduled task, no polling, no message broker integration |

---

## 17. Observability Audit

| Component | Status |
|---|---|
| Request ID propagation | **IMPLEMENTED** (RequestIdFilter + MDC) |
| Structured logging | **PARTIAL** (correlation pattern in application.yml) |
| Metrics | **MISSING** (no Micrometer, no Actuator) |
| Tracing | **MISSING** |
| Health checks | **MISSING** (no Spring Actuator) |
| Audit event service | **MISSING** (table exists, no code) |

---

## 18. Dependency Audit

**Production dependencies (pom.xml):**
| Dependency | Purpose | Status |
|---|---|---|
| spring-boot-starter-web | REST API | REQUIRED |
| spring-boot-starter-validation | Jakarta validation | REQUIRED |
| spring-boot-starter-data-jpa | JPA/Hibernate | REQUIRED |
| flyway-core | Database migrations | REQUIRED |
| flyway-database-postgresql | PostgreSQL Flyway dialect | REQUIRED |
| postgresql | PostgreSQL JDBC driver | REQUIRED |

**Test dependencies:**
| Dependency | Purpose | Status |
|---|---|---|
| h2 | In-memory test database | REQUIRED |
| spring-boot-starter-test | Testing framework | REQUIRED |

**Notable absences:**
- No `spring-boot-starter-security` — **Authentication cannot be implemented**
- No `jjwt` / `nimbus-jose-jwt` — No JWT capability
- No `spring-boot-starter-actuator` — No health/metrics
- No `spring-boot-starter-mail` — No email
- No AI SDK dependencies
- No Node.js / Prisma / BullMQ remnants (clean Java-only project)

---

## 19. Architecture Duplication Audit

| Concern | Instances | Status |
|---|---|---|
| Idempotency | 1 (`IdempotencyRecordEntity` reused across 3 services) | **CANONICAL** |
| Error handling | 1 (`GlobalExceptionHandler` + `CommonErrors`) | **CANONICAL** |
| ID generation | 1 (`UuidV7Generator`) | **CANONICAL** |
| Authorization | 1 (`ProjectAccessService`) | **CANONICAL** |
| Outbox | 1 (`OutboxEvent` + `OutboxEventRepository`) | **CANONICAL** |

**VERDICT: No duplicate architecture found. CLEAN.**

---

## 20. Master Reconciliation Matrix

| Domain | Master Requirement | Repository Status | Evidence |
|---|---|---|---|
| Authentication | Full auth system (registration, login, JWT, sessions) | **MISSING** | No auth code; schema tables exist |
| Authorization | RBAC with tenant hierarchy | **PARTIAL** | `ProjectAccessService` exists; no SecurityFilterChain |
| Tenancy | Org→Workspace→Project hierarchy | **PARTIAL** | Entities exist; no management endpoints |
| Database | 30+ table schema | **IMPLEMENTED** | 36 tables across 4 migrations |
| Files | Full file system | **PARTIAL** | Metadata only; no physical storage |
| Notes | Full notes system | **IMPLEMENTED** | Full CRUD + versions + knowledge |
| Chat | Full chat system | **PARTIAL** | Phase 1 persistence only (11/34 criteria) |
| Knowledge | Knowledge graph | **MISSING** | Only `knowledge_candidates` table |
| Documents | Document AI pipeline | **MISSING** | Schema tables only |
| Search | Full-text + semantic search | **MISSING** | No search implementation |
| AI Gateway | Provider abstraction + routing | **MISSING** | Schema tables only |
| Cognitive Runtime | Unified AI intelligence | **MISSING** | Not implemented |
| Research | Research workflows | **MISSING** | Not implemented |
| Evidence/Provenance | Scientific provenance | **PARTIAL** | `EvidenceStatus` enum only |
| Observability | Logging, metrics, tracing | **PARTIAL** | Request ID only |
| Audit | Audit trail | **MISSING** | Schema table only |
| Jobs/Workers | Async processing | **MISSING** | Schema tables only |
| Frontend | React/Next.js application | **MISSING** | No frontend exists |

---

## 21. Dependency Graph

```
Authentication (MISSING ←— BLOCKER)
↓
Security Context (MISSING)
↓
Tenant Context (PARTIAL — ProjectAccessService exists)
↓
Projects (IMPLEMENTED)
↓
Notes / Files / Chat (IMPLEMENTED — persistence layer)
↓
Document Processing (MISSING)
↓
Knowledge (MISSING)
↓
Search / Retrieval (MISSING)
↓
AI Gateway (MISSING — schema only)
↓
Cognitive Runtime (MISSING)
↓
Research Workflow (MISSING)
↓
Scientific Validation (MISSING)
```

---

## 22. Candidate Capability List

| # | Candidate | Evidence-Based Rationale |
|---|---|---|
| 1 | **Authentication & Security Foundation** | All controllers use `@RequestAttribute("userId")` but nothing populates it. Every API endpoint is effectively unauthenticated. Schema has users/sessions/refresh_tokens tables. |
| 2 | Physical file storage integration | FileService does metadata only; no upload/download |
| 3 | AI Gateway service layer | Schema exists but no entities/services |
| 4 | Outbox event publisher | OutboxEvent created but never published |
| 5 | Project management controller | ProjectCommandService exists but no REST controller |
| 6 | Spring Actuator / health checks | No observability tooling |

---

## 23. Candidate Ranking

| Candidate | Value | Dependency Benefit | Reuse | Cost | Risk | Verification | SCORE |
|---|---|---|---|---|---|---|---|
| Authentication | **CRITICAL** | Unlocks ALL secure API usage | HIGH (every controller) | MEDIUM | MEDIUM | HIGH | **#1** |
| Project controller | LOW | Convenience only | LOW | LOW | LOW | HIGH | #5 |
| Physical file storage | MEDIUM | Unlocks document pipeline | LOW | HIGH (infra) | MEDIUM | MEDIUM | #3 |
| AI Gateway services | MEDIUM | Unlocks AI features | MEDIUM | HIGH | HIGH | LOW | #4 |
| Outbox publisher | LOW | Enables async events | MEDIUM | LOW | LOW | HIGH | #6 |
| Actuator | LOW | Ops convenience | LOW | LOW | LOW | HIGH | #2 |

---

## 24. Minimum-Unlock Analysis

**Authentication Foundation** unlocks:
1. **Every existing endpoint** becomes usable in production
2. User registration and login
3. Session-based or JWT-based API access
4. Real tenant context propagation (userId populated from token)
5. Audit events (who did what)
6. Rate limiting (per-user)
7. All future capabilities requiring authenticated access

**Minimum implementation**: Registration + Login + JWT issuance + JWT verification filter + SecurityFilterChain + password hashing

---

## 25. Security Gate

| Check | Status |
|---|---|
| Authentication sufficient for next capability? | N/A — Authentication IS the next capability |
| Can be implemented safely? | YES — additive only |
| Existing data at risk? | NO — no production data exists |

---

## 26. Database Gate

| Check | Status |
|---|---|
| `users` table | EXISTS (V1) |
| `sessions` table | EXISTS (V1) |
| `refresh_tokens` table | EXISTS (V1) |
| `profiles` table | EXISTS (V1) |
| New migration required? | UNLIKELY — existing schema covers auth tables |
| Schema risk | LOW |

---

## 27. AI Gate

Authentication does not require AI. **NOT APPLICABLE.**

---

## 28. Tool Gate

Authentication does not require external tools. **NOT APPLICABLE.**

---

## 29. Scientific Integrity Gate

Authentication does not produce scientific output. **NOT APPLICABLE.**

---

## 30. Reliability Gate

| Check | Status |
|---|---|
| Idempotency | Not required for auth endpoints |
| Concurrency | Login/registration are naturally safe |
| Failure recovery | Standard HTTP error handling sufficient |
| Password hashing | Must use Argon2 or BCrypt (Master-specified) |

---

## 31. Cost Gate

| Resource | Estimate |
|---|---|
| CPU | LOW (password hashing is bounded) |
| Database | LOW (existing tables suffice) |
| External services | NONE |
| AI tokens | NONE |

---

## 32. Testability Gate

| Test Type | Feasible |
|---|---|
| Unit tests | YES |
| Integration tests | YES (H2 compatible) |
| Security tests | YES |
| Concurrency tests | YES (login race conditions) |
| Regression | YES (78 existing tests must pass) |

---

## 33. Selected Next Capability

**CAPABILITY: Authentication & Security Foundation**

---

## 34. Alternatives Rejected

| Candidate | Reason Rejected |
|---|---|
| Physical file storage | Requires auth first; cannot secure uploads without authentication |
| AI Gateway | Massive scope; authentication is prerequisite for any AI-facing API |
| Outbox publisher | Low value; does not unblock any user-facing capability |
| Project controller | Minor convenience; auth is more critical |
| Actuator | Observability is valuable but auth is the fundamental blocker |

---

## 35. Frozen Scope

### IN SCOPE
- User registration endpoint (POST /api/v1/auth/register)
- User login endpoint (POST /api/v1/auth/login)
- Password hashing (BCrypt or Argon2)
- JWT token issuance
- JWT verification filter (populates `@RequestAttribute("userId")`)
- Spring Security dependency + SecurityFilterChain
- Logout (token invalidation)
- Session/refresh token management (using existing tables)
- Auth-related DTOs (RegisterRequest, LoginRequest, AuthResponse)
- Auth tests (unit + integration)
- Regression: all 78 existing tests must continue passing

### OUT OF SCOPE
- OAuth / social login
- Email verification
- Password reset
- Multi-factor authentication
- Role-based endpoint restrictions (beyond userId propagation)
- Organization/workspace management endpoints
- Frontend authentication flow
- API rate limiting
- CORS configuration (may be minimal)

---

## 36. Next Capability Contract

| Field | Value |
|---|---|
| Capability | Authentication & Security Foundation |
| Purpose | Enable authenticated API access for all existing and future endpoints |
| User value | Users can register, login, and securely access their data |
| Dependencies | `spring-boot-starter-security`, JWT library (jjwt or nimbus) |
| Inputs | Registration: email + password; Login: email + password |
| Outputs | JWT access token, refresh token |
| Persistence | `users`, `sessions`, `refresh_tokens` (existing tables) |
| API | POST /auth/register, POST /auth/login, POST /auth/logout, POST /auth/refresh |
| Authentication | This IS the authentication capability |
| Authorization | SecurityFilterChain permits auth endpoints; requires JWT for all others |
| Tenant boundary | UserId extracted from verified JWT; propagated to service layer |
| Validation | Email format, password strength, duplicate email |
| Idempotency | Registration should be idempotent (same email → same user) |
| Concurrency | Concurrent registration with same email → exactly one user |
| AI usage | NONE |
| Tool usage | NONE |
| Audit | Login events should be logged |
| Observability | Auth failures should be logged with request ID |
| Failure handling | Invalid credentials → 401; duplicate email → 409; validation → 400 |
| Tests | Registration, login, invalid credentials, duplicate registration, JWT verification, expired token, refresh rotation, cross-user isolation |
| Definition of done | All existing 78 tests pass + new auth tests pass + endpoints return valid JWT + JWT filter populates userId for existing controllers |

---

## 37. Implementation Gate

**READY_FOR_IMPLEMENTATION**

All prerequisites are met:
- [x] Scope frozen
- [x] Dependencies identified (spring-boot-starter-security, jjwt)
- [x] Security defined (this IS security)
- [x] Persistence defined (existing tables)
- [x] API defined (4 auth endpoints)
- [x] Validation defined
- [x] Test strategy defined
- [x] Observability defined (request ID + auth logging)
- [x] Failure handling defined

---

## 38. Falsification Criteria

| Field | Value |
|---|---|
| Hypothesis | Adding authentication will make all existing API endpoints securely accessible |
| Baseline | Currently 0 endpoints are authenticated; all return errors or accept any userId |
| Expected improvement | 100% of endpoints require valid JWT; userId is cryptographically verified |
| Metric | All endpoints reject unauthenticated requests (401) and accept valid JWT |
| Failure condition | Any endpoint accessible without valid JWT, or JWT filter fails to populate userId |
| Falsification criterion | If a single existing endpoint can be called without authentication after implementation, the capability has FAILED |

---

## 39. Future File Contract

### CREATE
- `src/main/java/com/gsir/auth/AuthController.java` — Registration, login, logout, refresh endpoints
- `src/main/java/com/gsir/auth/AuthService.java` — Authentication business logic
- `src/main/java/com/gsir/auth/JwtService.java` — JWT creation and verification
- `src/main/java/com/gsir/auth/JwtAuthenticationFilter.java` — OncePerRequestFilter extracting JWT and setting userId
- `src/main/java/com/gsir/auth/SecurityConfig.java` — SecurityFilterChain configuration
- `src/main/java/com/gsir/auth/dto/RegisterRequest.java` — Registration DTO
- `src/main/java/com/gsir/auth/dto/LoginRequest.java` — Login DTO
- `src/main/java/com/gsir/auth/dto/AuthResponse.java` — Token response DTO
- `src/main/java/com/gsir/users/SessionEntity.java` — JPA entity for sessions table
- `src/main/java/com/gsir/users/SessionRepository.java` — JPA repository
- `src/main/java/com/gsir/users/RefreshTokenEntity.java` — JPA entity for refresh_tokens table
- `src/main/java/com/gsir/users/RefreshTokenRepository.java` — JPA repository
- `src/test/java/com/gsir/auth/AuthServiceTest.java` — Unit tests
- `src/test/java/com/gsir/auth/AuthIntegrationTest.java` — Integration tests
- `src/test/java/com/gsir/auth/JwtServiceTest.java` — JWT tests

### MODIFY
- `pom.xml` — Add spring-boot-starter-security, jjwt dependencies

### DO NOT TOUCH
- All existing entity, repository, service, controller files
- All existing migrations (V1–V4)
- All existing test files
- `GsirApplication.java`
- `application.yml` (except adding JWT secret configuration)

### MIGRATIONS
- NONE expected (tables already exist in V1)

### TESTS
- `AuthServiceTest.java` — Registration, login, password hashing
- `AuthIntegrationTest.java` — Full endpoint tests with JWT
- `JwtServiceTest.java` — Token creation, verification, expiration

---

## 40. Regression Baseline

| Component | Tests | Result |
|---|---|---|
| `GsirApplicationTests` | 1 | PASS |
| `DatabaseConfigurationTest` | 2 | PASS |
| `UuidV7GeneratorTest` | 6 | PASS |
| `NumericSafetyTest` | 13 | PASS |
| `StatisticsTest` | 14 | PASS |
| `AuditedEntityTest` | 2 | PASS |
| `EvidenceStatusTest` | 1 | PASS |
| `GlobalExceptionHandlerTest` | 7 | PASS |
| `ConversationServiceTest` | 3 | PASS |
| `ConversationServiceConcurrencyTest` | 1 | PASS |
| `FileServiceTest` | 5 | PASS |
| `FileServiceConcurrencyTest` | 1 | PASS |
| `NoteServiceTest` | 5 | PASS |
| `ProjectAccessServiceTest` | 8 | PASS |
| `ProjectCommandServiceTest` | 8 | PASS |
| `ProjectCommandServiceConcurrencyTest` | 1 | PASS |
| **TOTAL** | **78** | **PASS — BUILD SUCCESS** |

Executed: `.\mvnw.cmd clean test` — 78 passed, 0 failures, 0 errors, 0 skipped.

---

## 41. Known Blockers

1. **No git version control** — Cannot track changes, rollback, or audit modifications
2. **No authentication** — All endpoints are unauthenticated; blocks production deployment
3. **No Spring Security dependency** — Must be added to pom.xml before auth implementation

---

## 42. Known Limitations

1. Test database uses H2 in PostgreSQL compatibility mode with `ddl-auto: create-drop` and Flyway disabled — Flyway migrations are not tested against H2
2. No frontend exists — backend-only development
3. No Docker/deployment configuration
4. No CI/CD pipeline
5. `NoteAiService` appears to be a stub placeholder
6. Outbox events are created but never published (no event bus or poller)
7. 15 schema tables have no corresponding Java entities

---

## 43. Evidence Ledger

| Evidence | Source | Timestamp |
|---|---|---|
| File inventory: 79 production Java files | `find_by_name *.java` in `src/main/java` | 2026-09-14T19:43 |
| Test inventory: 16 test files, 78 tests | `find_by_name *.java` in `src/test/java` | 2026-09-14T19:45 |
| Schema: 36 tables across 4 migrations | Direct read of V1–V4 SQL files | 2026-09-14T19:43 |
| Build: 78 tests passed, BUILD SUCCESS | `.\mvnw.cmd clean test` actual execution | 2026-09-14T19:46 |
| No SecurityFilterChain | `grep_search SecurityFilterChain` → 0 results | 2026-09-14T19:45 |
| No JWT | `grep_search JWT` → 0 results | 2026-09-14T19:45 |
| No auth filter | `grep_search @RequestAttribute` → used in 30+ locations, never set | 2026-09-14T19:45 |
| pom.xml: no spring-boot-starter-security | Direct read of pom.xml | 2026-09-14T19:43 |
| No git repository | `git status` → fatal error in both directories | Session history |
| Spring Boot 3.4.3, Java 21 | pom.xml lines 10, 21 | 2026-09-14T19:43 |

---

## 44. Final Transition Record

| Field | Value |
|---|---|
| Audit completed by | Claude Opus 4.6 (Thinking) |
| Audit date | 2026-09-14 |
| Previous Part completed | Volume III Part 21 (Phase 1) — VERIFIED COMPLETE |
| Previous Part 22 | Logical Metadata — VERIFIED COMPLETE |
| Repository state | BUILD SUCCESS, 78/78 tests pass |
| Selected next capability | Authentication & Security Foundation |
| Implementation gate | READY_FOR_IMPLEMENTATION |
| Production code changed | NO |
| Database changed | NO |
| Test code changed | NO |
