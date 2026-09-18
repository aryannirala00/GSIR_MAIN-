# PART 99 — UNCERTAINTY PROPAGATION

## 1. Resolution
*   **Authoritative Master location:** Line 43268 of `GSIR_MASTER_CODING_DOCUMENT.md`. Found via exact trace.
*   **Numbering resolution:** A duplicate `PART 99` label exists for Governance, but the first logical match following Part 98 is `PART 99 — UNCERTAINTY PROPAGATION`. The title was retained exactly as specified.
*   **Master requirements:** Implement measurement uncertainty propagation for independent and correlated variables. Must retain `value`, `uncertainty`, `unit`, `method`, `assumptions`, and `covariance information where available`. Must not treat a number without uncertainty as exact.
*   **Extension trace:** Section 14.18 explicitly requires tracking uncertainty conceptually as `value ± uncertainty`. It explicitly prevents fabricating uncertainty. Types of uncertainty include `measurement_error`, `instrument_uncertainty`, `sampling_variability`, `biological_variability`, and `model_uncertainty`.

## 2. Mathematical Verification
*   **Mathematical propagation:** Covariance-aware mathematical propagation for addition (`u² = u1² + u2² + 2 cov`), subtraction (`u² = u1² + u2² - 2 cov`), multiplication, and division were rigorously mapped. The equations exactly follow standard error propagation (e.g. division uses `1/y` and `-x/y²` partial derivatives).
*   **Covariance behavior:** Checked via the Cauchy-Schwarz bound (`|cov| <= u1 * u2`). If the bound is violated, the implementation strictly throws an error. Correlated variances correctly apply the `2 * df/dx * df/dy * cov` term.
*   **Undefined uncertainty:** Handled perfectly. If either measurement lacks uncertainty, `UncertaintyPropagation` returns `Optional.empty()` rather than falling back to zero. Does not fabricate precision.
*   **Zero operands:** Division by zero is detected and strictly throws `IllegalArgumentException`.

## 3. Numerical Safety & Integrity
*   **Numerical safety:** Fully integrates with `NumericSafety`. `requireFinite` is run on all values and uncertainties to safely reject `NaN`, `+Infinity`, and `-Infinity`.
*   **Negative uncertainty:** Checked strictly. A negative uncertainty value is mathematically invalid and properly throws `IllegalArgumentException`. (Zero uncertainty is explicitly supported since it is a finite non-negative valid representation).
*   **Precision integrity:** Preserved because the uncertainty fields are completely independent of value, not inferred, and only set explicitly. 

## 4. Architecture Verification
*   **Database:** `V26__part_99_uncertainty.sql` applies cleanly and updates `experiment_observations` and `experiment_results`. Added `uncertainty`, `uncertainty_type`, `method`, `assumptions`, and `covariance_data` (TEXT for JSON). The `ExperimentObservationEntity` and `ExperimentResultEntity` were updated to match the JPA layer correctly.
*   **API:** `RecordObservationRequest` and `RecordResultRequest` accept the complete payload required by Master. Unnecessary internal details are not exposed. API acts as a pure conduit.
*   **Security:** `ExperimentService` restricts cross-project mutation via `getExperiment(experimentId, projectId, userId)`. Cross-project observation injection correctly fails with HTTP 403 FORBIDDEN.
*   **Tenant isolation:** Strictly tested. An authenticated user cannot mutate another project's experiment runs.
*   **Provenance:** Fully intact. The uncertainty fields are durably appended to the domain entities and the mutation is captured in canonical provenance via `ProvActivity`. Lineage is strictly unbroken.

## 5. System Properties
*   **Concurrency:** Mathematical logic (`UncertaintyPropagation`) is entirely pure and stateless, providing intrinsic thread-safety. Transactional boundaries on persistence guarantee database safety.
*   **Idempotency:** Pure calculation is strictly idempotent. Result saving relies on domain ID generation.

## 6. Execution Validation
*   **Failure handling:** Handled strictly via `GsirException` and standard exception mapping.
*   **Part 66 status:** Remains INCOMPLETE / MOCK. Data modeling of experimental runs is explicitly a stub and doesn't represent actual external robotics or autonomous test environments.
*   **Dedicated tests:** `UncertaintyPropagationTest` proves covariance handling, negative uncertainty, Cauchy-Schwarz violations, and independent limits. `ExperimentIntegrationTest` proves persistence.
*   **Repeatability:** Validated directly. Tests demonstrate no intermittent bleed.
*   **Full test result:** PASSED - 274 Tests.
*   **Full package result:** PASSED - Clean build SUCCESS without locking errors.

## 7. Status & Gap Audit
*   **Whole-GSIR gap status:** No other skipped dependencies detected. Core uncertainty mathematics successfully integrated into existing entities.
*   **Overall status:** PART 99 — VERIFIED COMPLETE.
*   **Next authoritative Part:** PART 100 — AGI + SCIENTIFIC RESEARCH LOOP.
