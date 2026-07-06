# Project Globals — Single Source of Truth

This file is injected into every agent context via the `instructions` field of
`opencode.json`. It complements AGENTS.md and must not duplicate its content.

## Java version

- Java 21 is the only supported version across the entire project.

## Model configuration

- Model slugs are defined EXCLUSIVELY in opencode.json under agent.<name>.model.
- Agent markdown frontmatter MUST NOT contain a model key.

## Maven execution policy

- Specialist engineer subagents (domain-engineer, persistence-engineer,
  web-engineer, frontend-engineer, test-engineer) NEVER invoke mvn directly.
- Their inner loop relies on LSP compile diagnostics, as configured by
  lsp: true in opencode.json.
- The orchestrator is the only agent that runs mvn, exactly once per layer,
  per the review gate defined in .opencode/commands/build.md.