---
description: Phase 3 - Build one hexagonal layer end-to-end with a per-layer two-stage review gate. Supports greenfield, extension and refactor modes.
agent: orchestrator
---
Parameters:
- $1 = LAYER (mandatory). Allowed values: domain | adapter-out | application | adapter-in | bootstrap | frontend | hardening.
- $2 = MODE (optional). Allowed values: greenfield | extension | refactor. Default: greenfield.
- $3 = REVIEW-CADENCE (optional). Allowed values: layer | step. Default: layer.

Resolution rules:
- If $1 is empty, abort and ask the user which layer to execute.
- If $2 is empty, treat MODE as greenfield.
- If $3 is empty, treat REVIEW-CADENCE as layer.

Execution model (all modes):
- Rely on LSP diagnostics for compile-level feedback while editing. Do NOT run `mvn compile` per micro-cycle.
- Dispatch parallelizable steps concurrently: for every set of steps whose "Can parallelize with" cells reference each other in the task-dispatch table, issue their specialist task calls in a single assistant message (cap: 4 concurrent specialists).
- Run `mvn -q test -pl <module>` once per layer, after all steps of the layer are implemented, as the authoritative compile+test gate.

Mode: greenfield
- Read @.opencode/plans/architecture-plan.md and @.opencode/plans/task-dispatch-table.md.
- Execute the steps for layer $1 in dependency order, dispatching the correct specialist subagent per the dispatch table. Parallelize independent steps per the Execution model.

Mode: extension
- Read @.opencode/plans/extension-plan.md and @.opencode/plans/task-dispatch-table.md.
- Read the impacted source files first (read-only) to map the current state and identify integration points.
- Apply the feature-extension-methodology skill.
- For every implementation step, the specialist subagent must:
    - prefer creating new files over modifying existing files;
    - when modifying an existing file is necessary (wiring a new bean, registering a new endpoint, extending a configuration, adding a new column via migration), edit additively;
    - preserve every public contract listed under the preserved-contract section of the extension plan (APIs, persistence schema, observable behaviors);
    - keep all existing tests green;
    - apply TDD to all newly written code (RED -> GREEN -> refactor) using the tdd skill, at class granularity;
    - perform the smallest reviewable change.

Mode: refactor
- Read @.opencode/plans/refactor-plan.md and @.opencode/plans/task-dispatch-table.md.
- Read the impacted source files first (read-only).
- Apply the refactoring-methodology skill: characterization tests first, then small reviewable steps invoking named patterns.
- For every implementation step, the specialist subagent must:
    - preserve every public contract listed under the non-regression section of the refactor plan;
    - keep all existing tests green;
    - add or extend tests as prescribed by the plan;
    - perform the smallest reviewable change, never bundle multiple refactorings.

Review gate:

When REVIEW-CADENCE is layer (default):
- After ALL steps of layer $1 are implemented and `mvn -q test -pl <module>` is green, review the whole layer changeset once.
    1. Dispatch spec-reviewer over the layer changeset.
        - APPROVED -> proceed to code-reviewer.
        - CHANGES_REQUESTED -> apply ONE corrective batch across all flagged steps, then re-review ONCE.
    2. Dispatch code-reviewer over the layer changeset.
        - APPROVED -> layer complete.
        - CHANGES_REQUESTED -> apply ONE corrective batch across all flagged steps, then re-review ONCE.
    3. If any reviewer still returns CHANGES_REQUESTED after its single corrective batch, STOP and escalate to the user with the outstanding items. Do not loop further.

When REVIEW-CADENCE is step (legacy):
- After each step:
    1. Dispatch spec-reviewer.
        - CHANGES_REQUESTED -> send feedback to implementer, fix, re-review.
        - APPROVED -> proceed.
    2. Dispatch code-reviewer.
        - CHANGES_REQUESTED -> send feedback to implementer, fix, re-review.
        - APPROVED -> step complete.

In extension mode, both reviewers must additionally verify that:
- the preserved contract is unchanged;
- pre-existing tests are still green;
- the change set is bounded to what the extension plan prescribes for that layer.