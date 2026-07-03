# Task Dispatch Table (Extension Business)

Source plan: `.opencode/plans/extension-plan.md` (Section 8 implementation steps only)

| Step number | Task description | Target subagent | Dependencies | Can parallelize with | Verification command(s) | Non-regression check | Mandatory review gates |
|---|---|---|---|---|---|---|---|
| 1 | **Layer 1 (domain)** — Add debtor/debt intake commands, inbound ports, outbound ports, and domain validation policies (UUID policy, allocatable statuses policy). | domain-engineer | None | 2, 4 | `mvn -q test -pl domain` | Run full existing domain tests | 1) `spec-reviewer` APPROVED, then 2) `code-reviewer` APPROVED |
| 2 | **Layer 2 (adapter-out)** — Add intake idempotency persistence (`IntakeRequest` entity/repo/gateway) and transactional workers for debtor/debt create **(edits-existing: persistence wiring/config/repositories)**. | persistence-engineer | 1; integrity of existing persistence wiring as-of start (`adapter-out` existing config/repositories) | 4 | `mvn -q test -pl adapter-out -Dtest='*Intake*Test,*Repository*Test'` | Existing adapter-out tests remain green | 1) `spec-reviewer` APPROVED, then 2) `code-reviewer` APPROVED |
| 3 | **Layer 2 (adapter-out)** — Implement global debt reference uniqueness migration and conflict mapping **(edits-existing: `DebtEntity`, migration scripts, repository checks)**. | persistence-engineer | 2; integrity of existing debt persistence/allocation mappings as-of start | None (conservative sequencing: shared persistence/migration surface) | `mvn -q test -pl adapter-out -Dtest='*Debt*Constraint*Test,*Concurrency*Test'` | Existing debt query/allocation persistence tests green | 1) `spec-reviewer` APPROVED, then 2) `code-reviewer` APPROVED |
| 4 | **Layer 2 (application)** — Add intake application services orchestrating ports and lifecycle audit emission (`*_REQUESTED/*_CREATED/*_REJECTED`). | domain-engineer | 1; integrity of existing allocation application services as-of start | 2 | `mvn -q test -pl application` | Existing allocation service tests green | 1) `spec-reviewer` APPROVED, then 2) `code-reviewer` APPROVED |
| 5 | **Layer 3 (adapter-in)** — Add debtor/debt write endpoints + DTOs + required header validation (`Idempotency-Key`, `X-Correlation-Id`) + permission checks **(edits-existing: optional existing debt-controller wiring)**. | web-engineer | 4, 3; integrity of existing payment/proposal/debt controllers and security entry points as-of start | 7 | `mvn -q test -pl adapter-in -Dtest='*Debtor*Controller*Test,*Debt*Controller*Test,*Security*Test'` | Existing payment/proposal controller tests green | 1) `spec-reviewer` APPROVED, then 2) `code-reviewer` APPROVED |
| 6 | **Layer 3 (bootstrap)** — Register new beans, security permission mappings, and observability meters/traces/log correlation for intake **(edits-existing: `ApplicationServiceConfig`, `application.yml`, security config)**. | web-engineer | 5, 3; integrity of existing bootstrap wiring/security/observability config as-of start | None | `mvn -q test -pl bootstrap -Dtest='*Context*Test,*Observability*Test'` | Existing bootstrap smoke tests green | 1) `spec-reviewer` APPROVED, then 2) `code-reviewer` APPROVED |
| 7 | **Layer 4 (frontend)** — Add debtor intake/list and debt intake pages + minimal navigation wiring **(edits-existing: existing shell/menu page)**. | frontend-engineer | 5; integrity of existing shell/menu static assets as-of start | 5 | `mvn -q test -pl bootstrap -Dtest='*UiSmoke*Test'` | Existing static page tests green | 1) `spec-reviewer` APPROVED, then 2) `code-reviewer` APPROVED |
| 8 | **Layer 5 (hardening)** — Add extension-focused concurrency, idempotency replay, intake-no-allocation-decoupling, and SLO alert-rule tests/checks **(edits-existing: test modules + monitoring config)**. | test-engineer | 6, 7; integrity of existing concurrency/integration test baselines and monitoring config as-of start | None | `mvn -q test -pl adapter-out,bootstrap -Dtest='*Concurrency*Test,*Idempotency*Test,*Integration*Test'` | Full regression suite green | 1) `spec-reviewer` APPROVED, then 2) `code-reviewer` APPROVED |

## Parallel groups

- **PG-1:** Steps **2** and **4** (after step 1 is green).
- **PG-2:** Steps **5** and **7** may overlap once step 5 API contracts are stable; no shared-file edits planned between adapter-in and frontend surfaces.

## Ordering policy applied

- Restricted strictly to extension implementation steps from extension plan section 8 (#1..#8), each included exactly once.
- Dependency-aware sequencing with conservative serialization on shared edits-existing persistence/bootstrap surfaces.
- Mandatory per-step review gate enforced in this order: **spec-reviewer -> code-reviewer**.
