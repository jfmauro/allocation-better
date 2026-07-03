---
name: feature-extension-methodology
description: Provides a deterministic methodology for adding new business functionality (new epics, features, or user stories) to an existing Spring Boot hexagonal codebase without breaking the existing contract. Use when the user runs /analyse extension-business, /plan extension-business, or /build with extension mode. Encodes the additive-first principle (prefer new files over edits to existing files), the preserved-contract principle (APIs, schema, behaviors remain unchanged), the integration-point discipline (existing files are touched only for wiring), and the TDD discipline for new code. Do not use for greenfield development, code review, or refactoring of existing behavior.
---

# Feature Extension Methodology

## Purpose

This skill drives safe addition of new business functionality on top of an existing codebase.

It applies when:

1. the project already exists and passes its baseline test suite;
2. a new business analysis is deposited under @knowledge/ describing a new epic, feature, or user stories;
3. the change is intended to add functionality, not to modify existing behavior.

It must not be used for greenfield code generation, for refactoring of existing behavior, or for technical-debt remediation. Those have their own dedicated skills.

## Primary mission

Produce extension artifacts that:

- preserve every public contract of the existing system unless an explicit decision says otherwise;
- prefer creating new files over editing existing ones;
- restrict edits to existing files to the minimum needed for wiring;
- keep all pre-existing tests green at every step;
- apply TDD to every newly written class;
- break the change into small, independently reviewable steps in hexagonal dependency order.

## Core principles

### 1. Additive first

A new feature should land primarily as new files: new entities, new ports, new services, new controllers, new pages.

Existing files are touched only when necessary, and only additively: new methods, new beans, new endpoints on an existing controller, new columns on an existing entity, new pages referenced from an existing navigation.

### 2. Preserved contract

Every extension artifact must contain a list of:

- preserved public APIs (HTTP endpoints with method, path, request shape, response shape, status codes);
- preserved persistence schema entries (tables, columns, indexes, constraints);
- preserved observable behaviors (ordering guarantees, idempotency keys, audit events, business invariants).

The list is the contract. Anything outside the list may evolve; anything inside must not.

### 3. Integration-point discipline

Existing-file edits are listed explicitly in the extension plan, with the marker "edits-existing", and each is justified:

- why the existing file must be touched (wiring, registration, migration);
- exactly what is added (method signature, bean declaration, endpoint mapping, column definition);
- what is NOT touched (lines preserved verbatim, methods that must remain unchanged).

### 4. Tests stay green, new code follows TDD

At every step:

- pre-existing tests must remain green;
- no test may be disabled, skipped, or weakened;
- new code is written test-first using the project tdd skill;
- characterization tests are added when an existing file is edited and its behavior intersects with the new feature.

### 5. Additive schema migrations

Schema changes introduced by the extension are additive by default:

- new tables;
- new nullable columns or columns with default values;
- new indexes;
- new foreign keys pointing FROM new tables to existing tables.

Destructive changes (drop column, change type, rename) require an explicit decision logged in the extension plan and an ADR in the documentation phase.

### 6. API versioning when needed

When a preserved endpoint must evolve incompatibly to support the new feature, the new behavior is exposed under a new path (versioned suffix or new resource), not as a breaking change to the existing endpoint. The deprecation plan is logged in the extension plan and in `docs/api.md` during the documentation phase.

## Runtime workflow

### Step 0 — Verify the knowledge directory layout

The framework expects this layout:

- knowledge/baseline/ - historical context (already covered by past cycles);
- knowledge/inbox/ - new scope to process during the current cycle.

Behavior:

- Read every file under knowledge/baseline/ as historical context.
- Read every file under knowledge/inbox/ as the authoritative new scope.
- If knowledge/inbox/ is empty, abort and ask the user to deposit the new business analysis file(s).
- If files exist at the root of knowledge/ (outside both subdirectories), treat them as baseline by default and emit a warning recommending migration to the proper subdirectory.

### Step 1 — Identify the new business scope

The contents of knowledge/inbox/ ARE the new scope, by convention. There is no
need to diff against the historical technical-analysis.md to detect novelty:
anything in inbox/ is new.

For each file in knowledge/inbox/:

- extract epics, features, user stories;
- check whether they primarily ADD functionality or primarily MODIFY existing behavior.

If more than 30 percent of the user stories primarily modify existing behavior, stop and recommend switching to refactor-business mode.

### Step 2 — Map the existing codebase

For each new user story, identify the existing modules, classes, endpoints, tables, and configuration entries that are candidates for integration:

- locate them precisely (module/path/file:line);
- for each, decide whether the new feature integrates by extension (new method/bean/endpoint) or by reference only (new code calls existing code unchanged);
- mark the items that must be touched as "edits-existing".

### Step 3 — Define the preserved contract

Produce the explicit list of:

- preserved public APIs;
- preserved persistence schema entries;
- preserved observable behaviors.

When in doubt, default to preserving.

### Step 4 — Design the new artifacts per user story

For each user story, produce a complete per-story analysis following the technical-analyst-builder skill template:

1. Context and Objective.
2. Detailed Functional Specifications.
3. API Contract.
4. Data Model.
5. Business Rules and Validations.
6. Error Management.
7. Edge Cases.
8. Dependencies (including the existing modules referenced).
9. Technical Acceptance Criteria.
10. UML Sequence Diagram.

### Step 5 — Plan additive schema migrations

If new tables or new columns are required:

- describe the migration script (Flyway / Liquibase or project equivalent);
- verify the migration is additive (or document the explicit decision otherwise);
- describe the backward-compatibility behavior during the deployment window;
- describe the rollback for the migration itself.

### Step 6 — Decompose into implementation steps

For each implementation step:

- name the target module (domain, application, adapter-out, adapter-in, bootstrap, frontend);
- describe what is added (new files preferred);
- describe edits to existing files when needed, with the "edits-existing" marker and the precise rationale;
- describe how to verify (tests to run, mvn command);
- describe non-regression check (which pre-existing tests must pass after this step).

Order steps following the hexagonal dependency flow:

1. domain (new entities, new ports);
2. adapter-out (new repositories, new transactional workers);
3. application (new services);
4. adapter-in (new endpoints, new DTOs, exception handler additions);
5. bootstrap (new bean wiring, new configuration entries);
6. frontend (new pages, modifications to existing navigation);
7. hardening (new concurrency tests if the feature has contention).

Within a layer, schedule new-files steps first, then edits-existing steps.

### Step 7 — Risk and mitigation

For each step, identify:

- the worst-case failure mode (regression, data corruption, broken backward compatibility);
- the detection mechanism;
- the mitigation;
- the rollback trigger.

## Output contract

Extension artifacts must follow:

- .opencode/templates/extension-analysis.md for the analysis;
- .opencode/templates/extension-plan.md for the plan.

## Self-quality standard

Before producing the final output, verify:

- preserved contract is explicit and complete;
- every existing-file edit is justified, marked, and bounded;
- new code is planned with TDD;
- schema changes are additive (or explicit decisions are logged);
- no step bundles new feature implementation with refactoring of existing code;
- the artifacts cite source files (path:line) for every integration point claim.