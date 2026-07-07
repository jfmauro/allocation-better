---
description: Phase 8 - Final consolidated release validation. Supports greenfield, extension and refactor modes, with optional SAD review.
agent: orchestrator
---
Parameters:
- $1 = MODE (optional). Allowed values: greenfield | extension | refactor. Default: greenfield.
- $2 = SAD_CHECK (optional). Allowed values: with-sad | no-sad. Default: with-sad.

Resolution rules:
- If $1 is empty, treat MODE as greenfield.
- If $2 is empty, treat SAD_CHECK as with-sad.
- If SAD_CHECK is with-sad but no SAD file is present under knowledge/, downgrade to no-sad and warn the user explicitly.

Artifact archival rule:
- Before writing .opencode/plans/release-report.md, if the file already exists, archive it under .opencode/plans/archive/v-<N>/<YYYYMMDD>T<HHMMSS>-release-report.md.
- Legacy root archive path is forbidden: .opencode/plans/archive/<YYYYMMDD>T<HHMMSS>-release-report.md.
- Request the archive version from the user exactly once per command execution with prompt prefix `V-`; the user provides the numeric suffix.
- Accept only integers in range 0..100000.
- Destination directory is .opencode/plans/archive/v-<N>/.
- If the selected .opencode/plans/archive/v-<N>/ already exists, ask explicit user confirmation before reuse.
- If the user does not confirm, request another version.
- Reuse the same confirmed v-<N> directory for all archive moves during the same command execution.
- Create the selected archive version directory if missing.
- Generate candidate output first and compare with the existing target file (if any).
- If candidate content is identical, skip archive and skip write, and log exactly: `No content change detected — no archive/write performed.`
- Do not execute ad-hoc prompts that redefine archival behavior; this command contract and the agent contract are authoritative.

Step 1 - Run full test suite:
!`mvn -q verify`

Step 2 - Architecture review:

If SAD_CHECK = with-sad:
- Dispatch sad-architect-reviewer to review the assembled system against:
  - @.opencode/plans/architecture-plan.md in greenfield mode;
  - @.opencode/plans/extension-plan.md (and the historical architecture-plan.md if present) in extension mode;
  - @.opencode/plans/refactor-plan.md (and the historical architecture-plan.md if present) in refactor mode.
- Returns APPROVED or CHANGES_REQUESTED.

If SAD_CHECK = no-sad:
- Skip the SAD-driven review.
- Dispatch sad-architect-reviewer in no-sad best-effort mode. Returns APPROVED, CHANGES_REQUESTED, or NO_SAD_BEST_EFFORT_OK.
- Add a banner to the release report: "WARNING: released without SAD validation."

Step 3 - Dispatch spec-reviewer once more against the whole codebase versus:
- @knowledge/baseline/ in greenfield mode;
- @knowledge/baseline/ AND @knowledge/inbox/ AND @.opencode/plans/extension-analysis.md in extension mode;
- @knowledge/baseline/ AND @knowledge/inbox/ AND @.opencode/plans/refactor-analysis.md in refactor mode.

In extension mode, spec-reviewer additionally verifies:
- the new features are fully implemented per extension-analysis.md;
- the preserved contract is unchanged;
- every pre-existing API listed as preserved is still present and behaves identically.

In refactor mode, spec-reviewer additionally verifies:
- the non-regression contract is preserved;
- every public API listed as preserved is still present and behaves identically.

Step 4 - Apply the archival rule on release-report.md if it exists.

Step 5 - Assemble the new release report at .opencode/plans/release-report.md:
- Mode.
- SAD review: with-sad / no-sad / downgraded-from-with-sad.
- Layer-by-layer completion status.
- Final mvn verify summary.
- Architect verdict.
- Spec-reviewer verdict.
- For extension: new features delivered checklist + preserved contract checklist.
- For refactor: non-regression status + preserved contract checklist.
- Open issues.
- Recommended next steps.
- Archive index: list of files archived during this cycle under .opencode/plans/archive/v-<N>/ and docs/archive/ (for traceability).

Step 6 - Present the report to the user for explicit release approval.

Step 7 - Post-release housekeeping (extension and refactor modes only):

If the user approves the release:
- Propose to promote the contents of knowledge/inbox/ into knowledge/baseline/ to prepare the next cycle.
- Wait for explicit user confirmation ("yes" / "no").
- If yes: move each file from knowledge/inbox/ to knowledge/baseline/, preserving file names. If a name collision occurs in baseline/, append the current timestamp to the moved file's name and warn the user.
- If no: leave knowledge/inbox/ untouched.

Skip Step 7 in greenfield mode (knowledge/inbox/ is typically empty after greenfield).
