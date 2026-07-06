# Project Globals — Single Source of Truth

This file is injected into every agent context via the instructions field of
opencode.json. It complements AGENTS.md and must not duplicate its content.

## Java version

- Java 21 is the only supported version across the entire project.

## Model configuration

- Model slugs are defined exclusively in opencode.json under agent.<name>.model.
- Agent markdown frontmatter must not contain a model key.

## Maven execution policy addendum

- Specialist engineer subagents never invoke mvn themselves, in any form.
- Only the orchestrator invokes mvn, exactly once per layer, per AGENTS.md
  and .opencode/commands/build.md.