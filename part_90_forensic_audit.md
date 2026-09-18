# PART 90 FORENSIC AUDIT

## 1. Authoritative title
**PART 90 — ACTION SELECTION**

## 2. Master lines
42856 - 42892

## 3. Numbering resolution
The single authoritative occurrence of "PART 90 — ACTION SELECTION" lies at line 42856. The second occurrence of "PART 90" (line 354750) is merely a compound section header cross-referencing Parts 87 and 88 ("GSIR RESEARCH DATA LIFECYCLE AND DATA GOVERNANCE ARCHITECTURE").

## 4. Extension trace
- **9.7 Action Selection**: Traced heuristics linking directly to the math implemented: expected utility, risk, cost.

## 5. Requirement matrix
Available in `part_90_gap_matrix.md`.

## 6. Repository baseline
Before implementation, GSIR executors processed tools without prior heuristic mathematical evaluation separating intent from risk.

## 7. Existing architecture reused
Integrated strictly into `com.gsir.ai.execution`. Evaluates actions logically before they enter the canonical execution stream.

## 8. Files added
- `C:\AI START UP\gsir-backend\src\main\java\com\gsir\ai\execution\ActionCandidate.java`
- `C:\AI START UP\gsir-backend\src\main\java\com\gsir\ai\execution\ActionSelector.java`
- `C:\AI START UP\gsir-backend\src\main\java\com\gsir\ai\execution\DefaultActionSelector.java`
- `C:\AI START UP\gsir-backend\src\test\java\com\gsir\ai\execution\Part90ActionSelectionTest.java`

## 9. Files modified
None.

## 10. Database changes
DATABASE — NOT REQUIRED

## 11. API changes
API — INTERNAL ONLY / NOT REQUIRED

## 12. Frontend status
FRONTEND — DEFERRED

## 13. Security verification
The Action Selector is a pure deterministic mathematical engine. As verified in the audit, artificial tenant database dependencies (`ProjectAccessService`) have been deliberately removed from the selector. Authorization is delegated up to the Orchestrator/Execution boundary, preventing unnecessary side-effects in pure math evaluation.

## 14. Tenant isolation
N/A to the mathematical engine; isolation remains enforced at the repository and orchestrator level.

## 15. Authorization
Delegated to caller.

## 16. Concurrency
The engine is completely stateless and pure. It is fully thread-safe for concurrent evaluation.

## 17. Idempotency
IDEMPOTENCY — NOT APPLICABLE / NO DURABLE SIDE EFFECT. As a pure mathematical utility, it has no persistent side-effects, hence idempotency concepts do not apply at this subsystem level.

## 18. Provenance/audit
Provenance for selected actions remains explicitly inherited from the execution orchestrator (Parts 80/83 canonical provenance). Selection itself is a transient mathematical decision and does not redundantly persist lineage.

## 19. Integration with Planning/Execution
Currently ISOLATED. `ActionSelector` is a modular service ready to be invoked by the agent orchestrator (Part 63 / 89 / 91) but does not duplicate or hack into the orchestration flow prematurely.

## 20. Dedicated Part 90 tests
`Part90ActionSelectionTest.java` successfully asserts:
- Exact `U(a) = B(a) - λR(a) - μC(a)` math correctness.
- Deterministic tie-breaking behavior (lexicographical tool fallback).
- Rejection of invalid constraints (NaN, Infinity).
- Empty/null candidate graceful handling.
- Enforcement of an arbitrary high-risk safety threshold (Implementation Policy).

## 21. Full regression
Tests run: 228
Failures: 0
Errors: 0
Skipped: 0

## 22. Exact package result
BUILD SUCCESS

## 23. Previous-Part regression
No regressions detected across Parts 14, 38, 39, 45, 63, 67, 78, 80-89.

## 24. Part 66 limitation
This boundary cleanly isolates physical execution from evaluation. Actual experiment execution remains INCOMPLETE / MOCK.

## 25. Deferred functionality
- Integration into the Tool Registry (Part 91).
- Authorization verification at the Orchestrator boundary.
