---
description: Phase 2 - Produce the architecture plan and the task dispatch table. Supports greenfield, extension-business and refactor modes, with optional SAD review.
agent: sad-architect-reviewer
subtask: true
---
Parameters:
- $1 = MODE (optional). Allowed values: greenfield | extension-business | refactor-business | refactor-technical. Default: greenfield.
- $2 = SAD_CHECK (optional). Allowed values: with-sad | no-sad. Default: with-sad.

Resolution rules:
- If $1 is empty, treat MODE as greenfield.
- If $2 is empty, treat SAD_CHECK as with-sad.
- If SAD_CHECK is with-sad but no SAD file is present under knowledge/, downgrade to no-sad and warn the user explicitly.

Artifact archival rule:
- Before writing any plan file under .opencode/plans/, if the target filename already exists, archive it under .opencode/plans/archive/v-<N>/<YYYYMMDD>T<HHMMSS>-<filename>.
- Request the archive version from the user exactly once per command execution with prompt prefix `V-`; the user provides the numeric suffix.
- Accept only integers in range 0..100000.
- Destination directory is .opencode/plans/archive/v-<N>/.
- If the selected .opencode/plans/archive/v-<N>/ already exists, ask explicit user confirmation before reuse.
- If the user does not confirm, request another version.
- Reuse the same confirmed v-<N> directory for all archive moves during the same command execution.
- Create the selected archive version directory if missing.
- Apply this rule to: architecture-plan.md, extension-plan.md, refactor-plan.md.
- Do NOT archive task-dispatch-table.md: it is regenerable from the plan and is always overwritten by the planner subagent.

Mode: greenfield
- Read @.opencode/plans/technical-analysis.md and @DESIGN.md.
- Apply the archival rule on architecture-plan.md.

Step 1 - Produce .opencode/plans/architecture-plan.md with:
1. Maven multi-module structure (domain, application, adapter-in, adapter-out, bootstrap).
2. Data model as a PlantUML entity diagram.
3. REST API endpoint table (method, path, request/response, status codes).
4. Locking strategy table (operation, lock type, implementation, rationale).
5. Sequence diagrams (PlantUML) for main write flows showing locking.
6. Frontend page table (page name, file, content).
7. Numbered implementation steps with columns: # | Step | Module | What | Test.
   Order steps following hexagonal dependency flow.

Apply the spring-boot-hexagonal-architecture skill.

Step 2 - Hand off to the planner subagent to produce .opencode/plans/task-dispatch-table.md (overwrite).

Step 3 - Present the two artifacts to the user for explicit approval before /build is allowed.

Mode: extension-business
- Read @.opencode/plans/extension-analysis.md (mandatory; abort if missing and ask the user to run /analyse extension-business first).
- Read @.opencode/plans/architecture-plan.md if present (historical reference).
- Read @.opencode/plans/technical-analysis.md if present (historical reference).
- Read @DESIGN.md.
- Apply the archival rule on extension-plan.md.

Step 1 - Produce .opencode/plans/extension-plan.md following the template at @.opencode/templates/extension-plan.md.

Apply the feature-extension-methodology skill and the spring-boot-hexagonal-architecture skill.

Step 2 - Hand off to the planner subagent to produce .opencode/plans/task-dispatch-table.md (overwrite), restricted to the extension steps.

Step 3 - Present the two artifacts to the user for explicit approval before /build is allowed.

Mode: refactor-business or refactor-technical
- Read @.opencode/plans/refactor-analysis.md (mandatory; abort if missing).
- Read @.opencode/plans/architecture-plan.md if present.
- Read @.opencode/plans/technical-analysis.md if present.
- Read @DESIGN.md.
- Apply the archival rule on refactor-plan.md.

Step 1 - Produce .opencode/plans/refactor-plan.md following the template at @.opencode/templates/refactor-plan.md.

Apply the refactoring-methodology skill and the spring-boot-hexagonal-architecture skill.

Step 2 - Hand off to the planner subagent to produce .opencode/plans/task-dispatch-table.md (overwrite) restricted to the refactor steps.

Step 3 - Present the two artifacts to the user for explicit approval before /build is allowed.

In all modes:

If SAD_CHECK = no-sad:
- Operate in no-sad best-effort mode (apply generic hexagonal and SOLID standards instead of a SAD).
- Write a clear warning at the top of the produced plan: "WARNING: produced without SAD validation."
- Recommend producing a SAD when budget allows.
