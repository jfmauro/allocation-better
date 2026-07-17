# Task Dispatch Table — Extension Implementation (from extension-plan §8)

Scope is strictly limited to extension implementation steps listed in `.opencode/plans/extension-plan.md` section 8.

## Layer 1 — Domain

| Step number | Task description | Target subagent | Dependencies | Can parallelize with | Verification command/check | Non-regression check |
|---|---|---|---|---|---|---|
| 1 | Add `AccountingEntry` domain model + value objects/enums (`AccountingEventType`, `SourceAggregateType`) with immutability and guard clauses (new-files) | domain-engineer | None | None | Domain unit tests for invariants/construction (e.g. `mvn -q test -pl domain`) | Existing domain tests remain green |
| 2 | Add accounting ports: outbound `AccountingEntryRepository` and inbound `AccountingEntryQueryUseCase` (new-files) | domain-engineer | 1 | 3, 6 | Port contract/unit tests with mocks (e.g. `mvn -q test -pl domain`) | Existing domain contracts unchanged |

## Layer 2 — Parallel track (Adapter-out + Application)

| Step number | Task description | Target subagent | Dependencies | Can parallelize with | Verification command/check | Non-regression check |
|---|---|---|---|---|---|---|
| 3 | Add accounting persistence stack: JPA entity, Spring Data repository, mapper, repository implementation (new-files) | persistence-engineer | 2 | 6 | `@DataJpaTest` for insert/query/order/filter + index-aware access paths (e.g. `mvn -q test -pl adapter-out`) | Existing adapter-out repository tests green |
| 4 | Wire strict-blocking `DEBT_ARRIVAL` insert in debt intake transactional worker (**edits-existing**: `adapter-out/.../JpaDebtIntakeTransactionalWorker.java`) | persistence-engineer | 3; baseline integrity of existing `JpaDebtIntakeTransactionalWorker` at build start | 5, 7, 8 | Success path creates exactly one `DEBT_ARRIVAL`; accounting insert failure triggers full rollback (e.g. `mvn -q test -pl adapter-out`) | Existing debt intake behavior unchanged |
| 5 | Wire strict-blocking `PAYMENT_ALLOCATION` insert in allocation transactional worker (**edits-existing**: `adapter-out/.../JpaAllocationTransactionalWorker.java`) | persistence-engineer | 3; baseline integrity of existing `JpaAllocationTransactionalWorker` at build start | 4, 7, 8 | Concurrency/transaction tests: one `PAYMENT_ALLOCATION` per successful allocation; rollback on accounting failure (e.g. `mvn -q test -pl adapter-out`) | Existing allocation lock/idempotency tests green |
| 6 | Add accounting application services (write + read query), including filter validation (`fromDate <= toDate`) (new-files) | domain-engineer | 2 | 3 | JUnit5/Mockito orchestration + validation tests (e.g. `mvn -q test -pl application`) | Existing application service tests green |
| 7 | Wire payment intake to create `PAYMENT_ARRIVAL` with `occurredAt = bankDate` (**edits-existing**: `application/.../PaymentIntakeApplicationService.java` or delegated worker wiring) | domain-engineer | 6, 3; baseline integrity of existing payment intake flow at build start | 4, 5, 8 | Tests assert `occurredAt=bankDate`; failed accounting write means no successful payment completion (e.g. `mvn -q test -pl application`) | Existing payment intake contract tests green |

## Layer 3 — Web (Adapter-in + Bootstrap wiring)

| Step number | Task description | Target subagent | Dependencies | Can parallelize with | Verification command/check | Non-regression check |
|---|---|---|---|---|---|---|
| 8 | Add `GET /accounting-entries` controller, DTOs, mapper, validation, and `ACCOUNTING_READ` enforcement at endpoint level (new-files) | web-engineer | 6, 3 | 4, 5, 7 | `@WebMvcTest`: 200/400/403, sort/filter behavior, empty list response (e.g. `mvn -q test -pl adapter-in`) | Existing controller tests unchanged |
| 9 | Register beans and security mapping for accounting read path (**edits-existing**: `bootstrap/.../ApplicationServiceConfig.java` + security config) | web-engineer | 8; baseline integrity of existing bootstrap security/bean graph at build start | None | Startup/wiring checks + security mapping tests (e.g. `mvn -q test -pl bootstrap`) | Existing bean graph and security behavior unaffected |

## Layer 4 — Frontend

| Step number | Task description | Target subagent | Dependencies | Can parallelize with | Verification command/check | Non-regression check |
|---|---|---|---|---|---|---|
| 10 | Add accounting entries UI page + JS (new-files) and add navigation link (**edits-existing**: `bootstrap/src/main/resources/static/index.html`) | frontend-engineer | 8, 9; baseline integrity of existing `index.html` navigation at build start | 11 | Manual browser validation of filters/table/empty state against `GET /accounting-entries`; smoke check navigation links | Existing pages remain functional |

## Layer 5 — Hardening

| Step number | Task description | Target subagent | Dependencies | Can parallelize with | Verification command/check | Non-regression check |
|---|---|---|---|---|---|---|
| 11 | Add/extend transactional + idempotency tests for all three accounting triggers and strict rollback semantics (**edits-existing** test suites in adapter-out/application) | test-engineer | 4, 5, 7 (and 8, 9 for end-to-end read-path confidence) | 10 | Run impacted module suites once per completed layer: `mvn -q test -pl domain`, `mvn -q test -pl application`, `mvn -q test -pl adapter-out`, `mvn -q test -pl adapter-in`, `mvn -q test -pl bootstrap` | Pre-existing full suite for impacted modules remains green |
