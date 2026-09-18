# PART 86 REQUIREMENT GAP MATRIX

| Requirement | Master Line | Existing Implementation | Status | Action Required |
| :--- | :--- | :--- | :--- | :--- |
| **86.3 Module & API** | 350410 | `src/main/java/com/gsir/experiments` | **COMPLETE** | API and module structured exactly to 86.49 |
| **86.4 Experiment Object** | 350438 | `ExperimentEntity` | **COMPLETE** | JPA entity maps relational data |
| **86.5 Experiment Status** | 350456 | `ExperimentStatus` enum | **COMPLETE** | Enum handles lifecycle |
| **86.6-86.9 Design, Variables, Params** | 350472 | DB tables exist | **COMPLETE** | `V23` handles relational design constraints |
| **86.10-86.11 Protocol & Versioning** | 350510 | `ExperimentProtocolEntity` | **COMPLETE** | Created entity and API |
| **86.12 Resources** | 350531 | Handled via constraints | **COMPLETE** | N/A |
| **86.14-86.16 Runs & Reproducibility** | 350556 | `ExperimentRunEntity` | **COMPLETE** | Created entity and API |
| **86.17-86.20 Observations & Data** | 350587 | `ExperimentObservationEntity` | **COMPLETE** | Created entity and API |
| **86.27-86.30 Results & Validation** | 350810 | `ExperimentResultEntity` | **COMPLETE** | Created entity and API |
| **86.34 Hypothesis Linking** | 350860 | DB column mapped | **COMPLETE** | Mapped `hypothesis_id` |
| **86.37 Provenance** | 350889 | Triggered in Service | **COMPLETE** | Integrated with `ProvenanceRepository` |
| **86.45 Safety** | 350949 | Safety logic mapped | **COMPLETE** | Mapped in Protocol |
| **86.46 Access Control** | 350963 | `ProjectAccessService` | **COMPLETE** | Verified via Security tests |
| **86.49 APIs** | 350993 | `ExperimentController` | **COMPLETE** | Implemented CRUD and lifecycle |
| **86.51 Database Structure** | 351030 | `V23__part_86_experiments.sql` | **COMPLETE** | DB schema fully applied |
| **Part 66 Constraint** | N/A | Execution remains mocked | **COMPLETE** | Run management distinct from execution |

## Strategy
1. **Database schema (`V23__part_86_experiments.sql`)**: Create `experiments`, `experiment_protocols`, `experiment_runs`, `experiment_variables`, `experiment_parameters`, `experiment_observations`, `experiment_results`.
2. **Entities**: JPA definitions for all above tables. Use `projectId` on `ExperimentEntity` for tenant isolation.
3. **Services**: Implement `ExperimentManagementService` that handles CRUD, state transitions, versioning, and provenance logging via `ProvenanceRepository`.
4. **API**: `ExperimentController` with proper `@AuthenticationPrincipal UUID userId` and `ProjectAccessService` integration.
5. **Security**: Add `KnowledgeControllerSecurityTest`-style endpoint integration tests to verify access denial.

