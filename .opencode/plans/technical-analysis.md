# Technical Analysis — Debtor/Debt Intake + Payment Allocation

**Trigger mode:** greenfield  
**SAD check:** with-sad  
**Date:** 2026-06-30  
**Author:** technical-functional-analyst

## 1. Scope split (baseline vs inbox)

### 1.1 Baseline (historical context — do not re-implement)

- Existing payment-allocation business scope and full matching/allocation lifecycle from Confluence `7078052229` (fetched from `knowledge/baseline/confluence-list-page-id.md`). [source: knowledge/baseline/confluence-list-page-id.md] [source: confluence:7078052229]
- SAD baseline for payment allocation from Confluence `7091912737` (fetched from `knowledge/baseline/confluence-list-page-id.md`). [source: knowledge/baseline/confluence-list-page-id.md] [source: confluence:7091912737]
- Knowledge contract and precedence from baseline README. [source: knowledge/baseline/README.md]

### 1.2 Inbox (current-cycle authoritative scope)

- New business scope: Debtor and Debt Intake lifecycle from Confluence `7117963683` (fetched from `knowledge/inbox/confluence-list-page-id.md`). [source: knowledge/inbox/confluence-list-page-id.md] [source: confluence:7117963683]
- SAD v2 addendum including debtor/debt intake in-scope from Confluence `7116980899` (fetched from `knowledge/inbox/confluence-list-page-id.md`). [source: knowledge/inbox/confluence-list-page-id.md] [source: confluence:7116980899]

## 2. Consolidated target scope

The target solution has two entry flows:
1) **Reference intake flow**: create debtor, create debt linked to existing debtor, enforce validation/duplicate prevention, audit intake events, and make eligible debts available for future matching. [source: confluence:7117963683] [source: confluence:7116980899]
2) **Payment-driven allocation flow**: receive payment, strict-priority matching (structured → identifier → name), automatic allocation only for valid/unambiguous structured communication, proposal + manual validation for all non-structured methods, effective allocation, balances update, and audit. [source: confluence:7078052229] [source: confluence:7091912737] [source: confluence:7116980899]

Hard boundary: debtor/debt intake **must never trigger allocation**. Allocation remains payment-triggered only. [source: confluence:7117963683] [source: confluence:7116980899] [source: confluence:7091912737]

## 3. Functional breakdown by epic and stories

### Epic A — Debtor and Debt Intake (new)

User stories in scope:
- **US-DI-001 / US-DI-002**: create debtor with mandatory data and duplicate prevention.
- **US-DI-003**: create debt only for existing debtor, with positive amount and valid data.
- **US-DI-004**: no matching/allocation trigger from intake actions.
- **US-DI-005**: eligible newly created debt becomes discoverable for later matching.
- **US-DI-006**: full intake audit trail. [source: confluence:7117963683]

Added SAD requirements FRQ-020..FRQ-027 confirm the same intake behavior and constraints. [source: confluence:7116980899]

### Epic B — Payment Allocation (baseline)

User stories US-001..US-019 remain valid and unchanged in core logic:
- strict matching priority,
- structured-only auto-allocation,
- mandatory manual validation for identifier/name,
- atomic allocation updates,
- overpayment policy,
- concurrency controls,
- privacy + audit constraints. [source: confluence:7078052229] [source: confluence:7091912737]

## 4. Domain and process architecture implications

## 4.1 Domain capabilities

Required bounded capabilities:
- Debtor intake management
- Debt intake management
- Payment intake
- Matching orchestrator
- Proposal management
- Manual validation UI/service
- Effective allocation engine
- Audit logging [source: confluence:7091912737] [source: confluence:7116980899]

### 4.2 End-to-end process contract

1. Debtor/debt intake writes reference data and audit only.
2. Payment intake starts matching flow.
3. Structured success may allocate automatically.
4. Structured failure falls back to identifier then name; both produce proposal only.
5. User decision on proposal gates effective allocation.
6. Allocation is atomic and auditable. [source: confluence:7078052229] [source: confluence:7091912737] [source: confluence:7116980899]

## 5. API contract baseline (implementation-ready)

### 5.1 Intake APIs (new in SAD v2)
- `POST /debtors`
- `GET /debtors/{id}`
- `GET /debtors`
- `POST /debts`
- `GET /debts/{id}`
- `GET /debtors/{id}/debts` (also used by allocation flow)

Rule: intake APIs must not invoke allocation execution. [source: confluence:7116980899]

### 5.2 Payment allocation APIs (existing baseline)
- `POST /payments`, `GET /payments/{id}`, `POST /payments/{id}/match`
- `POST /payments/{id}/match/structured-communication`
- `POST /payments/{id}/match/identifier`
- `POST /payments/{id}/match/name`
- `GET /allocation-proposals/{id}`
- `POST /allocation-proposals/{id}/validate`
- `POST /allocation-proposals/{id}/reject`
- `POST /allocations`, `GET /allocations/{id}` [source: confluence:7091912737] [source: confluence:7078052229]

## 6. Data model and invariants

Core entities: `Debtor`, `Debt`, `Payment`, `AllocationProposal`, `AllocationProposalCandidate`, `PaymentAllocation`, `AuditEvent`, `NationalNumberAccessLog`. [source: confluence:7091912737] [source: confluence:7117963683]

Critical invariants:
- No debt without existing debtor.
- Intake duplicate prevention for debtor and debt.
- `payment.remainingAmount >= 0`.
- `debt.remainingAmount >= 0` unless explicit overpayment policy path.
- No duplicate payment intake by transaction reference.
- No duplicate effective allocation under retries/concurrency. [source: confluence:7091912737] [source: confluence:7116980899] [source: confluence:7078052229]

## 7. Security, privacy, and audit

- RBAC for intake and allocation actions.
- National number masked by default; full display requires permission + reason + audit.
- IBAN masked by default.
- Mandatory audit events now include debtor/debt intake events plus matching/allocation events. [source: confluence:7091912737] [source: confluence:7116980899] [source: confluence:7117963683]

## 8. Non-functional baseline

- Deterministic business validation.
- Idempotency and concurrency safety.
- Atomic allocation transaction semantics.
- Horizontal scalability for API/workers.
- Full observability of intake, matching, proposal, allocation, and audit failures. [source: confluence:7091912737] [source: confluence:7116980899]

## 9. Technical acceptance criteria (summary)

1. Debtor creation enforces mandatory fields and duplicate policy.
2. Debt creation requires existing debtor and valid opening values.
3. Intake actions do not trigger matching/allocation.
4. Structured communication remains sole automatic allocation path.
5. Identifier/name paths never allocate directly.
6. Manual validation enforces authorization, reason, and valid selection/amount.
7. Allocation updates are atomic and concurrency-safe.
8. Intake + allocation audit trails are complete and queryable. [source: confluence:7117963683] [source: confluence:7116980899] [source: confluence:7078052229] [source: confluence:7091912737]

## 10. Open questions

1. What are the final authoritative duplicate rules (matching keys) for debtor and debt intake? [source: confluence:7117963683] [source: confluence:7116980899]
2. What is the production persistence choice and final transaction-boundary model for debt balance authority? [source: confluence:7091912737]
3. What default overpayment policy is mandated per business domain at go-live? [source: confluence:7091912737]
4. Is allocation cancellation endpoint/flow in this delivery scope, and what legal constraints apply? [source: confluence:7091912737]
5. What final volume/SLO figures must size intake and allocation workloads independently? [source: confluence:7091912737] [source: confluence:7116980899]

## 11. Readiness assessment

- **Readiness level:** Ready with minor clarifications.
- **Main blockers:** unresolved duplicate-rule detail for intake; unresolved debt authoritative boundary decision; unresolved default overpayment policy.
- **Recommended next actions:** close open decisions above, then proceed to planning with explicit API and data-contract freeze.
