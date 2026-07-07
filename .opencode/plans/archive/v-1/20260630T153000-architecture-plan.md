# Architecture Plan - Payment Allocation to Debts (archived)

## 1) Maven multi-module structure

```text
allocation/
├── pom.xml                          # parent aggregator
├── domain/
│   └── pom.xml
├── application/
│   └── pom.xml
├── adapter-in/
│   └── pom.xml
├── adapter-out/
│   └── pom.xml
└── bootstrap/
    └── pom.xml
```

Dependency flow (strict):
- `domain`: no Spring/JPA/web dependencies.
- `application` -> `domain` only.
- `adapter-in` -> `application` (+ `domain` for command/value mapping only).
- `adapter-out` -> `domain` (implements outbound ports, owns transactions).
- `bootstrap` -> all adapters + application for wiring/runtime.

## 2) Data model (PlantUML entity diagram)

```plantuml
@startuml
hide circle
skinparam linetype ortho

entity "payment" as payment {
  * id : UUID
  --
  bank_transaction_reference : String <<UK>>
  amount : Decimal
  remaining_amount : Decimal
  currency : String
  status : PaymentStatus
  structured_communication : String?
  free_communication : String?
  payer_name : String?
  payer_iban_masked : String?
  version : Long
  created_at : Instant
  updated_at : Instant
}

entity "debtor" as debtor {
  * id : UUID
  --
  type : DebtorType
  display_name : String
  national_number_hash : String?
  national_number_encrypted : String?
  enterprise_number : String?
  active : Boolean
  created_at : Instant
}

entity "debt" as debt {
  * id : UUID
  --
  debtor_id : UUID <<FK>>
  reference : String
  original_amount : Decimal
  remaining_amount : Decimal
  currency : String
  status : DebtStatus
  due_date : LocalDate?
  version : Long
  created_at : Instant
  updated_at : Instant
}

entity "allocation_proposal" as proposal {
  * id : UUID
  --
  payment_id : UUID <<FK>>
  status : ProposalStatus
  matching_method : MatchingMethod
  reason : String?
  validated_by : String?
  validated_at : Instant?
  version : Long
  created_at : Instant
  updated_at : Instant
}

entity "allocation_proposal_candidate" as candidate {
  * id : UUID
  --
  proposal_id : UUID <<FK>>
  debtor_id : UUID <<FK>>
  debt_id : UUID <<FK>>
  confidence : MatchConfidence
  suggested_amount : Decimal
  rank_order : Integer
}

entity "payment_allocation" as allocation {
  * id : UUID
  --
  payment_id : UUID <<FK>>
  debt_id : UUID <<FK>>
  proposal_id : UUID? <<FK>>
  amount : Decimal
  status : AllocationStatus
  idempotency_key : String <<UK>>
  command_id : String
  created_by : String
  created_at : Instant
}

entity "audit_event" as audit {
  * id : UUID
  --
  aggregate_type : String
  aggregate_id : UUID
  event_type : String
  actor : String?
  payload_json : CLOB
  created_at : Instant
}

entity "national_number_access_log" as nisslog {
  * id : UUID
  --
  payment_id : UUID?
  debtor_id : UUID
  user_id : String
  reason : String
  created_at : Instant
}

debtor ||--o{ debt
payment ||--o{ proposal
proposal ||--o{ candidate
debtor ||--o{ candidate
debt ||--o{ candidate
payment ||--o{ allocation
debt ||--o{ allocation
proposal ||--o{ allocation
payment ||--o{ audit
debt ||--o{ audit
proposal ||--o{ audit
debtor ||--o{ nisslog
@enduml
```

## 3) REST API endpoint table

| Method | Path | Request | Response | Status codes |
|---|---|---|---|---|
| POST | `/payments` | `ReceivePaymentRequest` | `PaymentResponse` | `201`, `400`, `409` |
| GET | `/payments/{id}` | Path `id` | `PaymentDetailsResponse` | `200`, `403`, `404` |
| POST | `/payments/{id}/match` | Path `id` | `MatchResultResponse` | `200`, `202`, `404`, `409` |
| POST | `/payments/{id}/match/structured-communication` | Path `id` | `StructuredMatchResponse` | `200`, `202`, `400`, `404`, `409` |
| POST | `/payments/{id}/match/identifier` | Path `id` | `ProposalCreationResponse` | `200`, `202`, `400`, `404` |
| POST | `/payments/{id}/match/name` | Path `id` | `ProposalCreationResponse` | `200`, `202`, `400`, `404` |
| GET | `/payments/{id}/proposals` | Path `id` | `AllocationProposalListResponse` | `200`, `404` |
| GET | `/allocation-proposals/{id}` | Path `id` | `AllocationProposalResponse` | `200`, `403`, `404` |
| POST | `/allocation-proposals/{id}/validate` | `ValidateProposalRequest` | `AllocationResultResponse` | `200`, `400`, `403`, `404`, `409` |
| POST | `/allocation-proposals/{id}/reject` | `RejectProposalRequest` | `ProposalStateResponse` | `200`, `400`, `403`, `404`, `409` |
| POST | `/allocation-proposals/{id}/select-debt` | `SelectDebtRequest` | `ProposalStateResponse` | `200`, `400`, `403`, `404`, `409` |
| POST | `/allocation-proposals/{id}/mark-unmatched` | `MarkUnmatchedRequest` | `ProposalStateResponse` | `200`, `400`, `403`, `404`, `409` |
| POST | `/allocation-proposals/{id}/request-investigation` | `RequestInvestigationRequest` | `ProposalStateResponse` | `200`, `400`, `403`, `404`, `409` |
| POST | `/allocations` | `ExecuteAllocationRequest` | `AllocationResultResponse` | `201`, `400`, `404`, `409` |
| GET | `/allocations/{id}` | Path `id` | `AllocationResponse` | `200`, `403`, `404` |
| GET | `/debtors/{id}/debts` | Path `id`, query filters | `DebtListResponse` | `200`, `403`, `404` |
| GET | `/debts/{id}` | Path `id` | `DebtResponse` | `200`, `403`, `404` |

## 4) Locking strategy table

| Operation | Lock type | Implementation | Rationale |
|---|---|---|---|
| Receive payment intake (`POST /payments`) | Unique constraint + optimistic | DB unique index on `bank_transaction_reference`; `@Version` on `Payment` | Idempotent intake and concurrent duplicate protection with low lock cost |
| Structured auto-allocation write path | Pessimistic write + optimistic recheck | `SELECT ... FOR UPDATE` / JPA `@Lock(PESSIMISTIC_WRITE)` on `Payment` + `Debt`; verify `@Version` and invariants before commit | Prevents double allocation and negative balances under contention |
| Manual proposal validation -> allocation | Pessimistic write + optimistic | Lock `Payment`, `Debt`, `AllocationProposal` rows in one transaction in adapter-out worker | Ensures one winning validator and atomic proposal/balance transitions |
| Proposal reject/select debt/mark unmatched/investigation | Optimistic | `@Version` on `AllocationProposal` | Prevents stale updates while avoiding heavy locks |
| Allocation command idempotency | Unique key lock-free dedupe | Unique index on `payment_id + debt_id + command_id` (or `idempotency_key`) | Retry-safe command execution without duplicate effective allocations |

Transaction placement:
- `@Transactional` only in `adapter-out` worker implementations of outbound ports.
- `application` orchestrates use cases and never owns transaction boundaries.

## 5) Sequence diagrams (main write flows with locking)

### 5.1 Structured communication auto-allocation

```plantuml
@startuml
title Structured auto-allocation (with lock scope)
actor Bank
participant "PaymentController\n(adapter-in)" as API
participant "PaymentApplicationService\n(application)" as APP
participant "StructuredMatchingDomainService\n(domain)" as SM
participant "AllocationTransactionalWorker\n(adapter-out, @Transactional)" as W
database "DB" as DB

Bank -> API: POST /payments
API -> APP: receivePayment(command)
APP -> W: saveReceivedPayment(payment)
W -> DB: INSERT payment(RECEIVED)
APP -> SM: matchStructured(payment)
SM --> APP: single eligible debt
APP -> W: executeEffectiveAllocation(paymentId,debtId,commandId)
W -> DB: SELECT payment FOR UPDATE
W -> DB: SELECT debt FOR UPDATE
W -> DB: check versions + invariants
W -> DB: INSERT payment_allocation
W -> DB: UPDATE payment remaining/status
W -> DB: UPDATE debt remaining/status
W -> DB: INSERT audit_event(s)
W -> DB: COMMIT
@enduml
```

### 5.2 Identifier/name proposal creation

```plantuml
@startuml
title Fallback proposal creation (no balance mutation)
participant "MatchingController\n(adapter-in)" as API
participant "MatchingApplicationService\n(application)" as APP
participant "IdentifierDomainService\n(domain)" as ID
participant "NameMatchingDomainService\n(domain)" as NM
participant "ProposalPersistenceWorker\n(adapter-out)" as W
database "DB" as DB

API -> APP: matchIdentifierOrName(paymentId)
APP -> ID: extractAndMatchIdentifiers(payment)
ID --> APP: no usable unique target
APP -> NM: matchByName(payment)
NM --> APP: candidate list
APP -> W: createProposal(paymentId,candidates)
W -> DB: INSERT allocation_proposal(PROPOSED)
W -> DB: INSERT proposal_candidates
W -> DB: UPDATE payment status MATCH_PROPOSED
W -> DB: INSERT audit_event(MATCH_PROPOSED)
@enduml
```

### 5.3 Manual validation to effective allocation

```plantuml
@startuml
title Manual validation allocation (with lock scope)
actor User
participant "AllocationProposalController\n(adapter-in)" as API
participant "ProposalApplicationService\n(application)" as APP
participant "AllocationTransactionalWorker\n(adapter-out, @Transactional)" as W
database "DB" as DB

User -> API: POST /allocation-proposals/{id}/validate
API -> APP: validateProposal(command, actor)
APP -> W: executeValidatedProposalAllocation(...)
W -> DB: SELECT proposal FOR UPDATE
W -> DB: SELECT payment FOR UPDATE
W -> DB: SELECT debt FOR UPDATE
W -> DB: check reason/permission/invariants/version
W -> DB: UPDATE proposal -> VALIDATED
W -> DB: INSERT payment_allocation
W -> DB: UPDATE payment remaining/status
W -> DB: UPDATE debt remaining/status
W -> DB: INSERT audit_event(USER_VALIDATED_ALLOCATION)
W -> DB: COMMIT
@enduml
```

## 6) Frontend page table

| Page name | File | Content |
|---|---|---|
| Payment Intake | `bootstrap/src/main/resources/static/payments/intake.html` | Intake form (reference, amount, currency, communications), client-side validation, success/error summary |
| Payment Detail & Match Timeline | `bootstrap/src/main/resources/static/payments/detail.html` | Payment state, matching attempts, masked sensitive fields, action buttons to trigger matching |
| Proposal Queue | `bootstrap/src/main/resources/static/proposals/list.html` | Filterable proposal list with status chips, priority sorting, workload counters |
| Payment Allocation Validation | `bootstrap/src/main/resources/static/proposals/validate.html` | Mandatory validation screen, candidate debts, reason input, validate/reject/investigation actions |
| Debt Search & Selection | `bootstrap/src/main/resources/static/debts/search.html` | Debtor debt list for selection, allocatable filters (`OPEN`, `PARTIALLY_PAID`) |
| Allocation Detail | `bootstrap/src/main/resources/static/allocations/detail.html` | Allocation result, payment/debt balance delta, audit trace summary |
| Audit & Sensitive Access Log | `bootstrap/src/main/resources/static/audit/access-log.html` | Full NISS access logs with reason, actor, date filters |

UI design constraints applied from `DESIGN.md`:
- PipelinePro tokens (`#4F46E5`, `#06B6D4`, `#F97316`), Outfit/Inter typography, 4px spacing scale.
- Validation page uses card/list/chip patterns and masked sensitive values by default.

## 7) Numbered implementation steps

| # | Step | Module | What | Test |
|---|---|---|---|---|
| 1 | Scaffold multi-module Maven project | root + all modules | Create parent aggregator, module poms, dependency management, baseline package structure, CI test profiles | `mvn -q test -pl domain,application,adapter-in,adapter-out,bootstrap` |
| 2 | Implement core domain models (TDD) | `domain` | Build `Payment`, `Debt`, `Debtor`, `AllocationProposal`, `PaymentAllocation` with invariants and state transitions | JUnit5 domain model tests per aggregate |
| 3 | Implement value objects and matching rules (TDD) | `domain` | Structured communication validator, NISS/BCE/VAT validators, name normalization/confidence logic | JUnit5 rule tests (checksum, normalization, confidence matrix) |
| 4 | Define inbound/outbound ports | `domain` | Define `port.in` use cases and `port.out` repository/gateway/transactional worker contracts | Compilation + unit tests with fake ports |
| 5 | Implement persistence entities/repositories/mappers | `adapter-out` | JPA entities with `@Version`, Spring Data repositories, MapStruct entity-domain mappers, DB constraints/indexes | `@DataJpaTest` for mappings, constraints, optimistic lock behavior |
| 6 | Implement transactional workers + locking | `adapter-out` | Outbound port implementations with `@Transactional`, `PESSIMISTIC_WRITE`, invariant rechecks, idempotency key handling, audit writes | `@DataJpaTest` + integration tests for lock contention and rollback |
| 7 | Implement application services orchestration | `application` | Use-case orchestration for intake/matching/proposal/validation/allocation; no transaction annotations | JUnit5 + Mockito service tests with mocked outbound ports |
| 8 | Implement REST controllers and DTO mappers | `adapter-in` | Thin controllers, request/response DTOs, Bean Validation, MapStruct DTO mappers | `@WebMvcTest` for endpoints, status codes, request validation |
| 9 | Implement global exception handler | `adapter-in` | `@RestControllerAdvice` for validation/business/conflict/security errors (Problem Details style) | `@WebMvcTest` for exception-to-HTTP mapping |
| 10 | Implement bootstrap wiring/configuration | `bootstrap` | Spring Boot app class, bean wiring, module composition, H2 local config, static resource routing | Boot smoke test + context load test |
| 11 | Implement frontend static pages | `bootstrap` | Build HTML/CSS/JS pages per table with PipelinePro styles and responsive behavior | UI smoke checks + basic JS interaction tests |
| 12 | Implement concurrency hardening tests | `adapter-out` + `bootstrap` tests | Concurrent allocation tests ensuring no duplicate allocation and no negative remaining amounts | Multi-thread integration tests (`CountDownLatch`, contention scenarios) |

Execution order is mandatory and follows hexagonal dependency flow:
1. scaffold
2. domain models (TDD)
3. ports
4. persistence
5. application services
6. REST controllers
7. exception handler
8. bootstrap config
9. frontend
10. concurrency tests
