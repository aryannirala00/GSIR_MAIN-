# PART 90 REQUIREMENT GAP MATRIX

| Requirement | Master Line | Extension Source | Implementation | Status |
| :--- | :--- | :--- | :--- | :--- |
| **90.1 ACTION SELECTION MODULE** | 42856 | 9.7 Action Selection | Pure mathematical engine decoupling AI intention scoring from orchestration execution. | **COMPLETE** |
| **90.2 ACTION CANDIDATE RECORD** | 42862 | 9.7 Action Selection | `ActionCandidate` tracks `tool`, `arguments`, `expectedUtility`, `risk`, `cost`. | **COMPLETE** |
| **90.3 POLICY SCORE FORMULA** | 42876 | 9.7 Action Selection | Implemented EXACT `U(a) = B(a) - λR(a) - μC(a)` in `DefaultActionSelector`, correctly bounding `NaN` and `Infinity` cases, rejecting invalid policy inputs. | **COMPLETE** |
| **90.4 MANDATORY AUTHORIZATION** | 42892 | 19.8 Stage 5 — Plan | Removed artificial `ProjectAccessService` coupling from the pure math engine. Authorization remains structurally DEFERRED to the Orchestration caller (Execution Engine), correctly preserving boundaries. | **DEFERRED** (to orchestration caller) |
| **90.5 SAFETY THRESHOLD** | 42892 | 9.7 Action Selection | Safety policy is implemented, explicitly rejecting candidates with Risk > 10.0 (marked as an IMPLEMENTATION POLICY as explicit bounds aren't given in the Master document). | **COMPLETE** |
