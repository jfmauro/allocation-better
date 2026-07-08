---
description: Phase 7 - Update or produce project documentation under docs/. Supports greenfield, extension and refactor modes.
agent: documentation-engineer
subtask: true
---
Parameters:
- $1 = MODE (optional). Allowed values: greenfield | extension | refactor. Default: greenfield.

Resolution rules:
- If $1 is empty, treat MODE as greenfield.

Mode: greenfield
- Read the codebase, @.opencode/plans/architecture-plan.md, @.opencode/plans/task-dispatch-table.md, and @knowledge/baseline/ + @knowledge/inbox/ for product context.
- Produce or update:
    - docs/README.md
    - docs/architecture.md
    - docs/api.md
    - docs/adr/NNNN-title.md (one ADR per significant architectural choice; assign the next free NNNN).

Mode: extension
- Read the codebase, @.opencode/plans/extension-plan.md, @.opencode/plans/extension-analysis.md, the historical @.opencode/plans/architecture-plan.md if present, and @knowledge/baseline/ + @knowledge/inbox/ for product context.
- Update (do not silently lose past content; preserve history):
    - docs/README.md only where the project description or run instructions changed.
    - docs/architecture.md to add the new modules, new ports, new data-model entries, and new flows; preserve unchanged sections.
    - docs/api.md to add the new endpoints clearly labelled as "added in extension <date or version>"; document any deprecation of existing endpoints if introduced by the extension.
    - docs/adr/NNNN-title.md: produce one new ADR per architectural decision specific to the extension (assign the next free NNNN); never overwrite an existing ADR.

Mode: refactor
- Read the codebase, @.opencode/plans/refactor-plan.md, @.opencode/plans/refactor-analysis.md, the historical @.opencode/plans/architecture-plan.md if present, and @knowledge/baseline/ + @knowledge/inbox/ for product context.
- Update:
    - docs/README.md only where the project description or run instructions changed.
    - docs/architecture.md to reflect new module boundaries, new ports, new locking strategy, or schema changes; preserve unchanged sections.
    - docs/api.md to reflect API additions, removals, or versioning; document deprecations.
    - docs/adr/NNNN-title.md: produce one new ADR per refactoring decision (assign the next free NNNN).

In all modes:
- Cite source files for every architectural claim.
- Do not modify source code.
- Return the list of created or modified files with a one-line summary each.
