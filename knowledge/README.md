# knowledge/

This directory holds every functional, contextual, and domain input
consumed by the agentic framework.

## Contract

- Any file dropped here is treated as authoritative input.
- Format-agnostic: Markdown, plain text, exported Confluence pages, HTML
  exports, or pre-extracted text from PDFs/DOCX are all accepted.
- Binary formats must be extracted to text or Markdown before being
  placed here.

## Precedence

When several files cover the same topic, list them below in decreasing
precedence; the technical-functional-analyst follows that order to
resolve overlaps.

## Inventory

| File                                 | Purpose                    | Notes |
|--------------------------------------|----------------------------|-------|
| (example) funreq.md                  | Functional requirements    | |
| (example) confluence-export.md       | Exported Confluence page   | |
| (example) confluence-list-page-id.md | List of Confluence page ID | |
| (example) regulatory.md              | Regulatory constraints     | |

If confluence-list-page-id.md is present, consult the Confluence page with that ID.

## What does NOT belong here

- Source code.
- Architectural decisions produced by the agents (those live in
  `.opencode/plans/` and `docs/adr/`).
- Test outputs and build artifacts.