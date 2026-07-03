---
name: refactoring-methodology
description: Provides a deterministic methodology for refactoring an existing codebase safely. Use when the user runs /analyse refactor-business, /analyse refactor-technical, /plan with a refactor mode, or /build with a refactor mode. Encodes the safety-net-first principle (characterization tests before refactoring), the small-steps principle (one reviewable refactoring at a time), the non-regression contract principle (explicit list of preserved APIs and behaviors), and recognized refactoring patterns. Do not use for greenfield development, pure code review, or business analysis.
---

# Refactoring Methodology

## Purpose

This skill drives safe refactorings of an existing Spring Boot hexagonal codebase.

It applies in three situations:

1. a business change request triggers structural changes to existing code;
2. a technical-debt assessment triggers structural improvements without business change;
3. a quality issue detected during code review requires a follow-up refactoring.

It must never be used for greenfield code generation.

## Primary mission

Produce refactoring artifacts that:

- preserve every public contract unless the plan explicitly says otherwise;
- start from a green test suite and end on a green test suite at every step;
- apply named, recognized refactoring patterns instead of ad-hoc rewrites;
- break the change into small, independently reviewable steps;
- document a rollback path for every step.

## Core principles

### 1. Safety net first

Before any structural change, characterization tests must exist for the impacted scope. If they do not, the first refactoring step is "add characterization tests" and no behavior change happens during that step.

Characterization test: a test that asserts the current behavior of the system as it is, not as it should be. Its purpose is to catch any change in observable behavior caused by the refactoring.

### 2. Small steps

Each refactoring step must be a single named refactoring or a single coherent atomic change. Bundling multiple refactorings in one step is forbidden.

Reviewable means:

- the diff fits in a single pull request a senior engineer can review in under 30 minutes;
- the change can be rolled back independently of subsequent steps.

### 3. Non-regression contract

Every refactoring artifact must contain a list of:

- preserved public APIs (HTTP endpoints with method, path, request shape, response shape, status codes);
- preserved persistence schema entries (tables, columns, indexes, constraints);
- preserved observable behaviors (e.g. ordering guarantees, idempotency keys, audit events).

The list is the contract. Anything outside the list may change; anything inside must not.

### 4. Tests stay green

At every step:

- pre-existing tests must remain green;
- no test may be disabled, skipped, or weakened to make the refactoring pass;
- new tests may be added.

If a pre-existing test must be changed because it asserted a now-invalid behavior, that behavior change must be explicitly listed in the refactor analysis as a delta, not hidden in the refactoring step.

### 5. Named patterns

Each refactoring step must invoke a named pattern from the recognized vocabulary. Examples (non-exhaustive):

- Extract Method;
- Inline Method;
- Extract Class;
- Inline Class;
- Move Method;
- Move Field;
- Rename;
- Replace Conditional with Polymorphism;
- Introduce Parameter Object;
- Replace Magic Number with Named Constant;
- Introduce Explaining Variable;
- Decompose Conditional;
- Replace Type Code with Subclasses;
- Encapsulate Field;
- Pull Up Method / Push Down Method;
- Replace Inheritance with Delegation;
- Strangler Fig (incremental replacement of a subsystem).

If no named pattern fits, the change is probably not a refactoring; flag it as a design change in the refactor analysis.

## Runtime workflow

### Step 0 — Verify the knowledge directory layout

The framework expects this layout:

- knowledge/baseline/ - historical context;
- knowledge/inbox/ - new scope (for refactor-business: holds the change request; for refactor-technical: typically empty).

Behavior:

- Read every file under knowledge/baseline/ as historical context.
- Read every file under knowledge/inbox/ as the authoritative new scope.
- For refactor-business: abort if knowledge/inbox/ is empty (the change request is required).
- For refactor-technical: knowledge/inbox/ may be empty (the codebase itself is the input).
- If files exist at the root of knowledge/ (outside both subdirectories), treat them as baseline by default and emit a warning recommending migration.

### Step 1 — Identify the trigger

Determine the trigger from the calling command:

- refactor-business: the change request file lives in knowledge/inbox/ (typically inbox/change-request.md, but any filename is accepted);
- refactor-technical: no business input; the trigger is a technical-debt assessment requested by the user.

### Step 2 — Map the impacted scope

Identify all modules, classes, methods, files, endpoints, and tables touched by the trigger.

For each item:

- locate it precisely (module/path/file:line);
- determine whether it is part of the non-regression contract or open to change;
- determine the test coverage state (covered, partially covered, uncovered).

### Step 3 — Define the non-regression contract

Produce the explicit list of:

- preserved public APIs;
- preserved persistence schema entries;
- preserved observable behaviors.

When in doubt, default to preserving.

### Step 4 — Plan the characterization tests

For each uncovered or partially covered item in the impacted scope:

- describe the characterization test to add;
- assign it to test-engineer in the dispatch table;
- mark it as a prerequisite to the refactoring step that depends on it.

### Step 5 — Decompose into refactoring steps

For each refactoring:

- name the recognized pattern;
- describe the target module, classes, methods;
- state why (rationale);
- state how to verify (tests to run, mvn command);
- state the rollback path.

Order steps following the hexagonal dependency flow when possible:

1. domain;
2. ports;
3. adapter-out;
4. application;
5. adapter-in;
6. bootstrap;
7. frontend;
8. hardening.

Within a layer, order by smallest blast radius first.

### Step 6 — Plan migrations if needed

If data migration or API versioning is required:

- describe the migration script;
- describe the backward-compatibility window;
- describe the cutover criteria;
- describe the rollback for the migration itself.

### Step 7 — Risk and mitigation

For each step, identify:

- the worst-case failure mode;
- the detection mechanism;
- the mitigation;
- the rollback trigger.

## Output contract

Refactoring artifacts must follow:

- .opencode/templates/refactor-analysis.md for the analysis;
- .opencode/templates/refactor-plan.md for the plan.

## Self-quality standard

Before producing the final output, verify:

- non-regression contract is explicit and complete;
- every step invokes a named refactoring pattern;
- every step has a rollback path;
- characterization tests are planned for every uncovered item in the impacted scope;
- no step bundles multiple refactorings;
- the artifacts cite source files (path:line) for every claim.