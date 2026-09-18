# GSIR Repository Capability Audit — Post-Part-22

**Model:** Claude Opus 4.6 (Thinking)  
**Date:** 2026-09-14  
**Scope:** Audit only — no implementation  
**Purpose:** Determine the single most appropriate next GSIR capability  

---

## 1. Audit Objective

Perform an evidence-backed audit of the actual GSIR repository state after the verified completion of Volume III Part 22 (Files — Logical Metadata Phase). Compare the repository against the authoritative Master Coding Document to identify IMPLEMENTED, PARTIAL, SCHEMA_ONLY, and MISSING capabilities, then select the single highest-priority minimum-unlock next capability.

---

## 2. Completed Part 22 Verification

**STATUS: VERIFIED COMPLETE**

| Check | Result |
|---|---|
| Validation | PASS |
| Idempotency | PASS |
| Concurrency | PASS |
| Tenant isolation | PASS |
| Schema | PASS |
| Database changes | NONE |
| Tests | 69 passed, 0 failed, 0 errors, 0 skipped |
| Full suite | PASS |
| Package | PASS |
| Repeatability | PASS |
| Acceptance criteria | 8/8 |
| Scope creep | PASS |

---

## 3. Current Repository Capability Matrix

**Evidence source:** Complete recursive inspection of `src/main/java/com/gsir/` (63 files), `src/test/java/com/gsir/` (14 files), and `src/main/resources/db/migration/` (3 files).

| CAPABILITY | SCHEMA | ENTITY | REPOSITORY | SERVICE | API | TESTS | STATUS |
|---|---|---|---|---|---|---|---|
| Users | ✅ V1 | ✅ UserEntity | ✅ UserRepository | ❌ | ❌ | ❌ | PARTIAL |
| Profiles | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Sessions | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Refresh Tokens | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Organizations | ✅ V1 | ✅ OrganizationEntity, OrganizationMembershipEntity | ✅ OrganizationRepository, OrganizationMembershipRepository | ❌ | ❌ | ❌ | PARTIAL |
| Workspaces | ✅ V1 | ✅ WorkspaceEntity, WorkspaceMembershipEntity | ✅ WorkspaceRepository, WorkspaceMembershipRepository | ❌ | ❌ | ❌ | PARTIAL |
| Projects | ✅ V1 | ✅ ProjectEntity | ✅ ProjectRepository | ✅ ProjectCommandService, ProjectAccessService | ❌ | ✅ 3 tests | PARTIAL |
| Idempotency | ✅ V2 | ✅ IdempotencyRecordEntity | ✅ IdempotencyRecordRepository | ✅ (used by ProjectCommandService, FileService) | N/A | ✅ | IMPLEMENTED |
| Outbox Events | ✅ V1 | ✅ OutboxEvent | ✅ OutboxEventRepository | ❌ (no publisher) | ❌ | ❌ | PARTIAL |
| Notes | ✅ V1+V3 | ✅ NoteEntity, NoteVersionEntity, NoteAttachmentEntity, NoteRelationshipEntity, KnowledgeCandidateEntity | ✅ 5 repositories | ✅ NoteService, NoteAiService, NoteKnowledgeService | ✅ NoteController | ✅ NoteServiceTest | IMPLEMENTED |
| Files (Metadata) | ✅ V1 | ✅ FileEntity, FileVersionEntity, StorageObjectEntity | ✅ 3 repositories | ✅ FileService | ✅ FileController | ✅ FileServiceTest, FileServiceConcurrencyTest | IMPLEMENTED |
| Conversations | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Messages | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Message Parts | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Web Links | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| AI Providers | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| AI Models | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| AI Model Versions | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Prompt Templates | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Prompt Versions | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| AI Runs | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| AI Outputs | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Document Extractions | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Document Chunks | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Jobs | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Job Attempts | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |
| Audit Events | ✅ V1 | ❌ | ❌ | ❌ | ❌ | ❌ | SCHEMA_ONLY |

---

## 4. Database Capability Matrix

**Migrations:**
- `V1__initial_schema.sql` — 30 tables (core platform schema)
- `V2__idempotency.sql` — 1 table (`idempotency_records`)
- `V3__notes_architecture.sql` — 5 tables + ALTER (notes extensions)

**Entity/Schema Alignment:**

| Table | Entity Exists | Mapping Correct |
|---|---|---|
| users | ✅ | ✅ |
| profiles | ❌ | N/A |
| sessions | ❌ | N/A |
| refresh_tokens | ❌ | N/A |
| organizations | ✅ | ✅ |
| organization_memberships | ✅ | ✅ |
| workspaces | ✅ | ✅ |
| workspace_memberships | ✅ | ✅ |
| projects | ✅ | ✅ |
| notes | ✅ | ✅ |
| note_versions | ✅ | ✅ |
| note_tags | ✅ (ElementCollection) | ✅ |
| note_relationships | ✅ | ✅ |
| note_attachments | ✅ | ✅ |
| knowledge_candidates | ✅ | ✅ |
| web_links | ❌ | N/A |
| conversations | ❌ | N/A |
| messages | ❌ | N/A |
| message_parts | ❌ | N/A |
| files | ✅ | ✅ |
| file_versions | ✅ | ✅ |
| storage_objects | ✅ | ✅ |
| document_extractions | ❌ | N/A |
| document_chunks | ❌ | N/A |
| ai_providers | ❌ | N/A |
| ai_models | ❌ | N/A |
| ai_model_versions | ❌ | N/A |
| prompt_templates | ❌ | N/A |
| prompt_versions | ❌ | N/A |
| ai_runs | ❌ | N/A |
| ai_outputs | ❌ | N/A |
| jobs | ❌ | N/A |
| job_attempts | ❌ | N/A |
| outbox_events | ✅ | ✅ |
| idempotency_records | ✅ | ✅ |
| audit_events | ❌ | N/A |

**Summary:** 20 of 36 tables have corresponding JPA entities. All existing entity mappings are correct. No unauthorized tables exist. No schema mismatches detected.

---

## 5. Security/Auth Status

**Current state: FOUNDATION ABSENT**

| Component | Status | Evidence |
|---|---|---|
| Spring Security dependency | ❌ MISSING | Not in `pom.xml` |
| JWT parsing | ❌ MISSING | No JWT-related files anywhere |
| Authentication filter | ❌ MISSING | Only `RequestIdFilter` exists (MDC logging only) |
| Session management | ❌ MISSING | `sessions` table exists but no entity/service |
| Token validation | ❌ MISSING | `refresh_tokens` table exists but no entity/service |
| User identity resolution | ❌ MOCK | Controllers use `@RequestAttribute("userId")` — no filter sets this |
| Authorization (data-layer) | ✅ IMPLEMENTED | `ProjectAccessService` with 404 masking |
| Tenant isolation | ✅ IMPLEMENTED | Multi-level hierarchy checks via `findAccessibleProject` |
| IDOR prevention | ✅ IMPLEMENTED | 404 response for unauthorized access (no existence oracle) |
| Password hashing | ❌ MISSING | `password_hash` column exists but no BCrypt/Argon2 logic |
| Audit logging | ❌ MISSING | `audit_events` table exists but no entity/service |

**Critical observation:** The `@RequestAttribute("userId")` pattern used by `NoteController` and `FileController` requires an upstream mechanism (filter, interceptor, or API gateway) to extract userId from a verified credential. No such mechanism currently exists in the codebase. In production, these APIs would receive null userId and fail.

---

## 6. AI Capability Status

**STATUS: MISSING**

- `NoteAiService` exists but uses hardcoded mock strings (`"AI Summary of: " + content`). No actual LLM invocation.
- No AI Gateway implementation. No provider adapters. No model router.
- V1 schema defines `ai_providers`, `ai_models`, `ai_model_versions`, `prompt_templates`, `prompt_versions`, `ai_runs`, `ai_outputs` — all SCHEMA_ONLY.
- `spring-boot-starter-webflux` is not in `pom.xml` (needed for streaming).
- No external AI SDK dependencies exist.

---

## 7. Notes Capability Status

**STATUS: IMPLEMENTED (Volume III Part 19)**

Complete implementation: NoteEntity, NoteVersionEntity, NoteAttachmentEntity, NoteRelationshipEntity, KnowledgeCandidateEntity, NoteService, NoteAiService (mock), NoteKnowledgeService, NoteController, NoteServiceTest. V3 migration verified.

---

## 8. Files Capability Status

**STATUS: IMPLEMENTED (Volume III Part 22 — Logical Metadata Phase)**

Complete implementation: FileEntity, FileVersionEntity, StorageObjectEntity, FileService (with idempotency), FileController (with `@Valid`), FileServiceTest, FileServiceConcurrencyTest.

**Deferred:** Physical binary storage (S3/MinIO), Document AI, OCR, extraction pipelines, chunk indexing, vector search.

---

## 9. Chat Capability Status

**STATUS: SCHEMA_ONLY**

V1 schema defines `conversations` (with `user_id`, `workspace_id`, `project_id`, `title`, `mode`, `status`), `messages` (with `conversation_id`, `parent_message_id`, `role`, `status`), and `message_parts` (with `message_id`, `part_type`, `content`, `metadata`, `sequence_number`). No Java entities, repositories, services, controllers, or tests exist.

---

## 10. Document/Knowledge Capability Status

**STATUS: SCHEMA_ONLY (except Knowledge Candidates via Part 19)**

- `document_extractions` and `document_chunks` tables exist in V1 — no Java implementation.
- `KnowledgeCandidateEntity` exists as part of the Notes subsystem (Part 19) — not the full Knowledge Graph (Part 23).

---

## 11. Job/Background Processing Status

**STATUS: PARTIAL**

- `OutboxEvent` entity and repository exist. `ProjectCommandService` writes outbox events transactionally.
- No outbox publisher/poller exists to actually process and deliver events.
- `jobs` and `job_attempts` tables exist in V1 — no Java implementation.
- No `@Scheduled`, no Spring Batch, no Quartz integration.

---

## 12. Master Part Implementation Status

| Volume | Part | Title | Status |
|---|---|---|---|
| II | 13–14 | Foundation, Idempotency, Transactions | IMPLEMENTED |
| II | 15–18 | Math, Statistics, Science utilities | IMPLEMENTED |
| III | 19 | Notes, Knowledge Candidates | IMPLEMENTED |
| III | 20 | Projects, Workspaces | PARTIAL (services exist, no API) |
| III | 21 | Chat, Conversations, Messages | SCHEMA_ONLY |
| III | 22 | Files (Logical Metadata Phase) | IMPLEMENTED |
| III | 23 | Knowledge Graph | MISSING |
| III | 24 | Document Intelligence | MISSING |
| III | 25 | Agent Orchestration | MISSING |
| III | 38 | API Security, Zero-Trust | MISSING |
| III | 43 | AI Gateway | MISSING |

---

## 13. Missing Capabilities

**Critical Missing (blocks production):**
1. Authentication / JWT / Session management (Part 38)
2. AI Gateway / Provider integration (Part 43)

**Feature Missing (blocks product):**
3. Chat / Conversations / Messages (Part 21)
4. Knowledge Graph (Part 23)
5. Document Intelligence / Extraction (Part 24)
6. Background job processing
7. Outbox event publisher
8. Audit event logging
9. Physical file storage (S3/MinIO)

---

## 14. Dependency Analysis

Evidence from Master Document (exact line references):

1. **Part 20 → Part 19**: "Project Notes shall use the Notes subsystem defined in Part 19." (§20.14, line 197836) — **SATISFIED**
2. **Part 20 → Part 22**: "Project Files shall use the File subsystem." (§20.15, line 197856) — **SATISFIED**
3. **Part 21 → AI Gateway**: "The Chat subsystem must remain modular and must communicate with the AI Gateway rather than directly implementing model-provider logic." (line 199214) — **NOT SATISFIED** for full implementation
4. **Part 24 → Part 23**: "Document-derived knowledge connects to Part 23 Knowledge Architecture." (§24.58, line 207363)
5. **Part 25 → Part 24**: "The agents defined in Part 25 shall use the Document Intelligence system defined in Part 24." (§25.61, line 209613)

**Dependency chain for Chat (full):** Part 21 → AI Gateway (Part 43) → Authentication (Part 38)

**Dependency chain for Chat (Phase 1 — message persistence only):** Part 21 Phase 1 → Foundation (SATISFIED) + Projects (SATISFIED)

---

## 15. Security Gate

**Question:** Does implementing the next capability require authentication to be safe?

**Analysis:**
- NoteController and FileController ALREADY use the `@RequestAttribute("userId")` pattern without authentication. This pre-existing security gap is not worsened by adding another controller with the same pattern.
- Implementing Chat message persistence (Phase 1) with the same pattern does NOT introduce new attack surface beyond what Notes and Files already expose.
- The AI Gateway (Part 43) WOULD require authentication because it connects to external paid LLM providers — billing abuse risk.

**Verdict:** DOES NOT BLOCK Phase 1 Chat persistence. DOES BLOCK AI Gateway.

---

## 16. Database Gate

**V1 schema already defines `conversations`, `messages`, and `message_parts` tables with:**
- Proper foreign keys to `users`, `workspaces`, `projects`
- Indexes on `user_id` and `project_id`
- Unique constraint on `(message_id, sequence_number)` for message parts
- `parent_message_id` self-referential FK for branching
- `mode` column for conversation modes
- `status` columns for lifecycle management

**Verdict:** PASS. No new migration required for Chat Phase 1.

---

## 17. Candidate Next Parts

| Candidate | Volume/Part | Dependencies Met | Schema Ready | Security Blocked | Complexity |
|---|---|---|---|---|---|
| Chat (Phase 1: Persistence) | III / 21 | ✅ Foundation, Projects, Notes, Files | ✅ V1 tables exist | ❌ Same risk as Notes/Files | Medium |
| API Security (Phase 1: JWT) | III / 38 | ✅ Foundation | ✅ sessions, refresh_tokens exist | N/A | Medium-High |
| Knowledge Graph | III / 23 | ❌ Requires document extraction | ❌ May need schema | ❌ Requires AI | High |
| AI Gateway | III / 43 | ❌ Requires authentication | ✅ ai_* tables exist | ✅ BLOCKED | High |

---

## 18. Recommended Next Capability

**Volume III Part 21 — Chat, Conversation Tabs, Message Architecture and Context Management**  
**Phase 1: Conversation and Message Persistence (Logical Metadata)**

---

## 19. Implementation Readiness

| Check | Status |
|---|---|
| Master specification exists | ✅ Part 21, lines 199166–201156 |
| Schema exists | ✅ `conversations`, `messages`, `message_parts` in V1 |
| Foundation dependencies met | ✅ Users, Organizations, Workspaces, Projects |
| Architectural pattern established | ✅ Same pattern as Part 19 (Notes) and Part 22 (Files) |
| Security not worsened | ✅ Same `@RequestAttribute("userId")` pattern |
| No new migration required | ✅ V1 tables are sufficient |
| Phase 1 is self-contained | ✅ Conversation/message CRUD without AI Gateway |

**VERDICT: READY**

---

## 20. Risks / Blockers

| Risk | Severity | Mitigation |
|---|---|---|
| No authentication exists | Pre-existing (applies to Notes + Files too) | Part 38 should follow Part 21 Phase 1 |
| Chat acceptance criteria require AI Gateway | Scoping risk | Explicitly scope Phase 1 as persistence only |
| `message_parts` uses JSONB metadata | Low | H2 supports JSONB in PostgreSQL mode |
| `parent_message_id` self-FK for branching | Low | Well-supported in JPA |

**BLOCKERS: NONE** (for Phase 1 scope)

---

## 21. Exact Next-Step Recommendation

**IMPLEMENT:** Volume III Part 21 — Chat, Conversation Tabs, Message Architecture and Context Management

**SCOPE:** Phase 1 — Conversation and Message Persistence (Logical Metadata Only)

**Phase 1 includes:**
- ConversationEntity, MessageEntity, MessagePartEntity
- ConversationRepository, MessageRepository, MessagePartRepository
- ConversationService (CRUD, project-scoped, user-scoped)
- ConversationController (REST API)
- Tenant isolation via ProjectAccessService (same pattern as Notes/Files)
- Idempotency for conversation creation (reuse Part 14 architecture)
- ConversationServiceTest, ConversationServiceConcurrencyTest
- Bean validation on DTOs

**Phase 1 explicitly defers:**
- AI response generation (requires AI Gateway)
- Streaming (requires WebFlux)
- Model selection (requires AI Gateway)
- Research mode (requires Agent Orchestration)
- Tools/Plugins (requires AI Gateway)
- Conversation memory (requires AI Gateway)
- Web search integration

**WHY Part 21 over Part 38 (Security):**
1. Part 21 follows the established Volume III sequential order (20 → **21** → 22 done)
2. Part 20 acceptance criteria explicitly require "Projects can contain Chats" (line 199114)
3. The schema already exists in V1 — zero migration risk
4. The security gap is pre-existing (Notes/Files already use the same unprotected pattern)
5. Chat persistence follows the exact same phased approach proven by Part 22 (Files)
6. The Master's Build Order §10 says "Audit the existing codebase first" and "Build one strong core first" — conversation persistence is core infrastructure
7. Part 38 should come NEXT (after Part 21 Phase 1) to secure all existing APIs simultaneously

**Database changes required:** NONE  
**New dependencies required:** NONE  
