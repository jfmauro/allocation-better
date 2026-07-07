# Project Globals — Single Source of Truth

This file is injected into every agent context via the instructions field of
opencode.json. It complements AGENTS.md and must not duplicate its content.

## Java version

- Java 21 is the only supported version across the entire project.

## Model configuration

- Model slugs are defined exclusively in opencode.json under agent.<name>.model.
- Agent markdown frontmatter must not contain a model key.

## Maven execution policy addendum

## Maven execution policy addendum

- Specialist engineer subagents never invoke mvn themselves, in any form,
  except test-engineer, which requires actual multi-threaded execution to
  verify concurrency invariants and may run mvn -q test -pl bootstrap
  directly for that purpose.
- Only the orchestrator invokes mvn for all other layers, exactly once per
  layer, per AGENTS.md and .opencode/commands/build.md.

## Plans archival policy

- `.opencode/plans/*.md` at the repository root is archived only by
  `/analyse extension-business`.
- The archive version must always be requested as `V-<n>` at the start of that
  command.
- Archived files use `.opencode/plans/archive/v-<n>/<YYYYMMDD>T<HHMMSS>-<filename>`.
