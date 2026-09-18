# PART 85 GAP MATRIX

| Requirement | Master Line | Extension | Existing Implementation | Existing Component | Status | Required Change | Verification Test |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Entities Represented | 85.5-85.6 | 12.x | `knowledge_entities`, `KnowledgeEntity` | `PostgresGraphStorageAdapter` | PARTIAL | Add tenant isolation | `KnowledgeControllerSecurityTest` |
| Relationships | 85.7-85.8 | 12.x | `knowledge_relationships`, `KnowledgeRelationship` | `PostgresGraphStorageAdapter` | PARTIAL | Add tenant isolation | `KnowledgeControllerSecurityTest` |
| Scientific Claims | 85.12-85.13| 12.x | `scientific_claims`, `ScientificClaim` | `PostgresGraphStorageAdapter` | PARTIAL | Add tenant isolation | `KnowledgeControllerSecurityTest` |
| Evidence Linked | 85.14-85.15| 12.x | Deferred in `DefaultKnowledgeService.attachEvidence` | - | MISSING | Implement `attachEvidence` | `KnowledgeEvidenceIntegrationTest` |
| Provenance | 85.16 | - | `provenance_records` | `ProvenanceRepository` | DUPLICATE / BROKEN | Remove `provenance_records` if it duplicates Part 80. Use `prov_activities`. | `KnowledgeProvenanceIntegrationTest` |
| Tenant Isolation & Security | 85.62, 85.23 | 12.x | `KnowledgeController` ignores `userId` | `ProjectAccessService` | BROKEN / MISSING | Inject `ProjectAccessService`, add `@AuthenticationPrincipal` to all endpoints | `KnowledgeControllerSecurityTest` |
| Entity Resolution | 85.10 | - | `EntityResolutionService` (mergeEntities) | - | PARTIAL | Verify it handles security correctly | `EntityResolutionSecurityTest` |
| Duplicate Detection | 85.39 | - | - | - | DEFERRED | N/A | - |
| Multi-hop reasoning | 85.28 | - | `KnowledgeGraphQueryEngine` | - | PARTIAL | Add `ProjectAccessService` check | `KnowledgeQuerySecurityTest` |
| Contradictions | 85.19, 85.60 | - | `ContradictionDetectionService` | - | PARTIAL | Add tenant check | `ContradictionSecurityTest` |
| Frontend | 85.72 | - | None found | - | DEFERRED | Record as DEFERRED | - |

**Duplication Note:** `V11__knowledge_graph.sql` contains `provenance_records`. This is a duplicate system! Part 80 implemented `prov_agents`, `prov_activities`, `prov_entities`, and `prov_relationships` via `V12__provenance_engine.sql`. The knowledge graph must NOT use a parallel `provenance_records` table if `prov_activities` exists! Wait, I cannot delete `V11` if Flyway has already run it, but I shouldn't use it.
Wait, let's see if `V11` can be dropped, or if I just ignore it and use Part 80 `ProvenanceRepository` like I did in Part 84.
In Part 84, I used `ProvenanceRepository`.
I will update `KnowledgeController` and `DefaultKnowledgeService` to enforce `ProjectAccessService.requireAccess(projectId, userId)` for all methods.
I'll also implement `attachEvidence` securely, and trigger a `ProvActivity` (type `KNOWLEDGE_ADDED`) using Part 80's `ProvenanceRepository`.

