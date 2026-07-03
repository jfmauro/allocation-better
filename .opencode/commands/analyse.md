---
description: Phase 1 - Produce a structured technical analysis from knowledge/. Supports greenfield, extension-business, refactor-business and refactor-technical modes. SAD review is optional.
agent: technical-functional-analyst
subtask: true
---
Parameters:
- $1 = MODE (optional). Allowed values: greenfield | extension-business | refactor-business | refactor-technical. Default: greenfield.
- $2 = SAD_CHECK (optional). Allowed values: with-sad | no-sad. Default: with-sad.

## Step 0 — Confluence pre-resolution (ALWAYS execute first, before any other step)

Before reading knowledge/ content or detecting the SAD, resolve all Confluence page IDs:

1. Check if any file named confluence-list-page-id.md exists under knowledge/baseline/ or knowledge/inbox/.
2. For each such file found, read every page ID it contains.
3. Fetch the content of each page via the Confluence MCP tool or the REST endpoint:
   GET <CONFLUENCE_BASE_URL>/rest/api/content/<page-id>?expand=body.storage
4. Convert fetched content to Markdown and treat it as an in-memory file named
   confluence-<page-id>.md, located in the same subdirectory as the source confluence-list-page-id.md.
5. A fetched page whose title or content contains "SAD", "Solution Architecture", or "Architecture Document"
   (case-insensitive) is treated as a SAD file for all subsequent SAD detection rules.
6. If a fetch fails, abort and report the failed page ID to the user. Do not proceed.
7. Log to the user: which IDs were fetched, which subdirectory (baseline or inbox), and whether any fetched page was identified as a SAD.

This step is unconditional. It runs in every mode, including greenfield.

## Step 1 — SAD detection (execute after Step 0)

SAD_CHECK resolution rules:
- If $1 is empty, treat MODE as greenfield.
- If $2 is empty, treat SAD_CHECK as with-sad.
- A SAD is considered present if ANY of the following is true:
  a. A file whose name contains "sad" (case-insensitive) exists under knowledge/baseline/ or knowledge/inbox/.
  b. A fetched Confluence page (from Step 0) was identified as a SAD.
- If SAD_CHECK = with-sad AND no SAD is present by the above criteria, downgrade to no-sad and warn the user explicitly with the message:
  "SAD not found. Checked: filenames under knowledge/baseline/ and knowledge/inbox/, and content of all fetched Confluence pages. Downgrading to no-sad."
- If SAD_CHECK = with-sad AND a SAD is present, proceed with with-sad.
- Files placed at the root of knowledge/ (outside both subdirectories) are treated as baseline by default with a backward-compatibility warning.

## Step 2 — Artifact archival (execute before writing any output file)

Before writing any analysis file under .opencode/plans/:
1. Check whether the target file already exists (technical-analysis.md, extension-analysis.md, or refactor-analysis.md depending on mode).
2. Always log the result of the check to the user:
  - "Found existing <filename> — archiving to .opencode/plans/archive/<YYYYMMDD>T<HHMMSS>-<filename>."
  - "No existing <filename> found — no archive needed."
3. If the file exists, move it to .opencode/plans/archive/<YYYYMMDD>T<HHMMSS>-<filename>. Create the archive/ directory if missing.
4. Only then write the new version.

This step is unconditional and mandatory. Never skip or assume the file is absent without checking.

## Step 3 — Analysis production

Knowledge directory convention:
- knowledge/baseline/ holds the historical context (already covered by past cycles).
- knowledge/inbox/ holds the new scope to process during the current cycle.
- Fetched Confluence pages (from Step 0) are treated as files in the subdirectory of their source confluence-list-page-id.md.

Mode: greenfield
- Read every file under @knowledge/ (baseline/, inbox/, root, and fetched Confluence pages).
- Apply the archival rule (Step 2) on .opencode/plans/technical-analysis.md.
- Produce .opencode/plans/technical-analysis.md.
- Use the technical-analyst-builder skill to drive the analysis.
- Cite source filenames inline (use confluence:<page-id> for fetched pages).
- After saving, summarize the open questions back to the user.

Mode: extension-business
- Read every file under @knowledge/baseline/ as historical context (including fetched Confluence pages from baseline/).
- Read every file under @knowledge/inbox/ as the authoritative new scope (including fetched Confluence pages from inbox/).
- Read .opencode/plans/technical-analysis.md if present, as the historical baseline.
- Read .opencode/plans/architecture-plan.md if present, as the architectural baseline.
- Read the existing codebase (read-only) to identify integration points and preserved contracts.
- Apply the archival rule (Step 2) on .opencode/plans/extension-analysis.md.
- Use the technical-analyst-builder skill in extension mode.
- Produce .opencode/plans/extension-analysis.md following the template at @.opencode/templates/extension-analysis.md.
- Do not overwrite .opencode/plans/technical-analysis.md.
- Do not modify the existing codebase.
- Abort and ask the user if @knowledge/inbox/ is empty (no files and no fetched pages).

Mode: refactor-business
- Read every file under @knowledge/baseline/ as historical context (including fetched Confluence pages from baseline/).
- Read every file under @knowledge/inbox/ as the authoritative new scope (including fetched Confluence pages from inbox/).
- Read the existing codebase (read-only).
- Read .opencode/plans/architecture-plan.md if present.
- Read .opencode/plans/technical-analysis.md if present.
- Apply the archival rule (Step 2) on .opencode/plans/refactor-analysis.md.
- Use the technical-analyst-builder skill in refactor mode and the refactoring-methodology skill.
- Produce .opencode/plans/refactor-analysis.md following the template at @.opencode/templates/refactor-analysis.md.
- Cite source filenames inline.
- Do not overwrite .opencode/plans/technical-analysis.md.
- Abort and ask the user if @knowledge/inbox/ is empty.

Mode: refactor-technical
- No business change request expected. @knowledge/inbox/ may be empty.
- Read every file under @knowledge/baseline/ as historical context.
- Read the existing codebase (read-only).
- Read .opencode/plans/architecture-plan.md if present.
- Read .opencode/plans/technical-analysis.md if present.
- Apply the archival rule (Step 2) on .opencode/plans/refactor-analysis.md.
- Use the refactoring-methodology skill.
- Produce .opencode/plans/refactor-analysis.md following the template at @.opencode/templates/refactor-analysis.md.
- Do not propose business changes.

## Step 4 — SAD review gate

If SAD_CHECK = with-sad:
- Invoke sad-architect-reviewer to review the analysis using the technical-analysis-sad-review skill.
- The reviewer returns APPROVED or CHANGES_REQUESTED.

If SAD_CHECK = no-sad:
- Invoke sad-architect-reviewer in no-sad best-effort mode.
- Write at the top of the produced analysis file:
  "WARNING: produced without SAD validation. Findings are best-effort against generic architecture standards."
- Inform the user and recommend producing a SAD when budget allows.

## Step 5 — Post-review user guidance (ALWAYS present to the user after Step 4)

Present the following status block to the user after every run, regardless of verdict:

```
## /analyse result

Mode:        <greenfield | extension-business | refactor-business | refactor-technical>
SAD check:   <with-sad | no-sad | downgraded-from-with-sad>
Archive:     <list of files moved to archive/ or "none">
Output:      <path of produced analysis file>
SAD review:  <APPROVED | CHANGES_REQUESTED | NO_SAD_BEST_EFFORT_OK | skipped>

## Next step

If SAD review = APPROVED or NO_SAD_BEST_EFFORT_OK:
  → You may run: /plan <mode> <sad-check>
  → Command to copy: /plan extension-business with-sad

If SAD review = CHANGES_REQUESTED:
  → /plan is BLOCKED until the review is resolved.
  → To resolve:
      1. Review the findings listed above by sad-architect-reviewer.
      2. Answer any open questions listed in the analysis.
      3. Run the following command to update the analysis and re-trigger the review:
         /analyse extension-business with-sad
  → Do NOT run /plan until this command returns APPROVED.

If SAD check = downgraded-from-with-sad:
  → Cause: no SAD file found in knowledge/baseline/, knowledge/inbox/, or fetched Confluence pages.
  → To fix: ensure the SAD page ID is listed in knowledge/baseline/confluence-list-page-id.md
    and that the page is accessible via Confluence MCP.
  → Re-run: /analyse extension-business with-sad
```