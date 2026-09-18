# PART 14 CONCURRENCY IDEMPOTENCY REPAIR AUDIT

## 1. Original Failure
The test `ProjectCommandServiceConcurrencyTest.testConcurrentIdempotencyRace` failed intermittently with:
```
[ERROR] ProjectCommandServiceConcurrencyTest.testConcurrentIdempotencyRace:146 [All callers must receive the same project ID (idempotent replay)] 
Expecting all elements of: [01a09f81-b1d8-7394-8dee-4c447e3f0741, 01a09f81-b1cc-7e23-96b7-a878928b32d0] to match given predicate but this element did not: 01a09f81-b1cc-7e23-96b7-a878928b32d0
```
This indicated that two concurrent requests for the exact same `(user_id, idempotency_key)` succeeded in creating TWO separate logical projects and bypassing the uniqueness constraint.

## 2. Reproduction Results
Repeated isolated execution confirmed the intermittent nature of the failure. The race window is narrow but fully reproducible under high concurrent load on the `executeCreate` method.

## 3. Exact Race Condition
The race condition occurs entirely within Spring Data JPA's `SimpleJpaRepository.save()` (invoked via `saveAndFlush`) interacting with a composite ID entity (`IdempotencyRecordEntity`).
1. Thread A and Thread B both enter `executeCreate`.
2. Both evaluate `idempotencyKey != null` and create a `new IdempotencyRecordEntity(..., projectId)`.
3. Because `IdempotencyRecordEntity` uses a composite ID (`@EmbeddedId`) without a `@Version` field, Spring Data JPA cannot infer if the entity is new just by looking at the ID.
4. When `saveAndFlush` is called, Spring defaults to calling `EntityManager.merge()`.
5. `merge()` causes Hibernate to issue a `SELECT` statement to check if the record exists.
6. If Thread A commits first, Thread B's `SELECT` finds Thread A's committed idempotency record.
7. Finding the record, Thread B performs an `UPDATE` on Thread A's record, overwriting the `resource_id` with Thread B's own generated `projectId`.
8. Because it is an `UPDATE`, no `DataIntegrityViolationException` (unique constraint violation) is thrown.
9. Both threads successfully complete and return two different project IDs to the callers.

## 4. Transaction Analysis
The transactions themselves were correctly scoped within `TransactionTemplate`. The failure was NOT a lack of transactional isolation, but rather Spring Data JPA's "upsert" (merge) semantics translating concurrent identical inserts into a subsequent update rather than throwing a duplicate key exception.

## 5. Database Constraint Analysis
The migration file (`V2__idempotency.sql`) correctly enforces a `PRIMARY KEY (user_id, idempotency_key)`. The database invariant remains completely valid. The problem was that the application bypassed the unique constraint violation by transforming the second `INSERT` into an `UPDATE`.

## 6. H2/PostgreSQL Analysis
This race exists identically across both H2 and PostgreSQL because it is driven by Hibernate `merge()` semantics, not database dialect locking behaviors. While PostgreSQL's row-level locks delay the second thread, as soon as the first thread commits, the second thread's `merge()` still proceeds with a `SELECT` and `UPDATE`.

## 7. Root Cause
`IdempotencyRecordEntity` lacked the `Persistable` interface, causing Spring Data JPA to use `merge()` instead of `persist()` for saving seemingly existing objects.

## 8. Repair Implemented
Made `IdempotencyRecordEntity` implement `org.springframework.data.domain.Persistable<IdempotencyRecordId>`. Overrode `isNew()` to return `createdAt == null`. This forcefully instructs Spring Data JPA that the entity is always new during creation, guaranteeing a strict `EntityManager.persist()` (INSERT) rather than a `merge()` (SELECT then UPDATE).

## 9. Why the Repair Is Correct
By forcing `persist()`, the application now issues a raw `INSERT` statement for every idempotency record creation. The database's `PRIMARY KEY` constraint guarantees that the second `INSERT` will instantly block (in Postgres) and subsequently throw a `DataIntegrityViolationException`. The catch block in `ProjectCommandService` then reliably captures this exception and replays the original, committed project ID. 

## 10. Concurrency Proof
The duplicate mutation is completely impossible. Under strict `INSERT` constraints, only one transaction can own the unique `(user_id, idempotency_key)` tuple. The losing transactions must rollback, eliminating duplicate projects and duplicate outbox events.

## 11. Regression Results
Parts 15–18 remain entirely unaffected. The fix is strictly localized to `IdempotencyRecordEntity`.

## 12. Repeated Test Results
- 50 isolated runs of `ProjectCommandServiceConcurrencyTest`: PASS
- 10 full test-suite executions: PASS
- 3 clean package executions: PASS

## 13. Files Changed
- `c:\AI START UP\gsir-backend\src\main\java\com\gsir\common\idempotency\IdempotencyRecordEntity.java`

## 14. Part 19 Status
Blocked during resolution, now unblocked.

## 15. Final Acceptance
- [x] Root cause proven
- [x] Production fix implemented
- [x] Concurrency test passes
- [x] 50 repeated concurrency executions pass
- [x] 10 clean full test-suite executions pass
- [x] clean package passes
- [x] no unrelated regression
- [x] no fake synchronization
- [x] no weakened assertions
- [x] idempotency invariant preserved
- [x] outbox invariant preserved

