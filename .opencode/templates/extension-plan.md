# Extension Plan

**Trigger mode:** extension-business
**SAD check:** with-sad | no-sad
**Date:**
**Author:** sad-architect-reviewer

> Warning block here when SAD_CHECK = no-sad:
> WARNING: produced without SAD validation.

## 1. Scope

Reference to .opencode/plans/extension-analysis.md.

Summary of new modules, new classes, new endpoints, new tables, and the minimal set of existing files touched for wiring.

## 2. Preserved contract

(Copied from extension-analysis.md, kept synchronized.)

### 2.1 Preserved public APIs

| Method | Path | Request shape | Response shape | Status codes |
|---|---|---|---|---|

### 2.2 Preserved persistence schema entries

| Entry | Reason |
|---|---|

### 2.3 Preserved observable behaviors

| Behavior | Reason |
|---|---|

## 3. Data model after extension

PlantUML entity diagram of the data model AFTER the extension (existing entities plus new entities and new columns).

Highlight new entries by adding a comment in the diagram.

## 4. REST API additions

| Method | Path | Request | Response | Status codes | Label (new / extended) |
|---|---|---|---|---|---|

## 5. Locking strategy for new flows

| Operation | Lock type | Implementation | Rationale |
|---|---|---|---|

## 6. Sequence diagrams (PlantUML)

For each new write flow, provide a PlantUML sequence diagram including the locking.

## 7. Frontend additions

| Page | File | Status (new / extended) | Content |
|---|---|---|---|

## 8. Implementation steps

| # | Layer | Step description | New files only? | Edits-existing (paths) | Subagent | How to verify | Non-regression check |
|---|---|---|---|---|---|---|---|

Order steps following the hexagonal dependency flow:

1. domain
2. adapter-out
3. application
4. adapter-in
5. bootstrap
6. frontend
7. hardening

Within a layer, schedule new-files steps before edits-existing steps when possible.

## 9. Migration plan

### 9.1 Data migration

- Tooling:
- Forward script (additive):
- Backward script (rollback):
- Backward-compatibility window:

### 9.2 API versioning

- Existing endpoints preserved as-is: yes / no
- New endpoints exposed at:
- Deprecation plan (if any):

### 9.3 Frontend impact

- New pages:
- Modified existing pages (for navigation only):
- Coordination notes:

## 10. Risks and mitigations

| Risk | Likelihood | Impact | Mitigation | Rollback trigger |
|---|---|---|---|---|

## 11. Approval

- [ ] User approval received before /build extension is executed.