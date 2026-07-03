# Refactor Plan

**Trigger mode:** refactor-business | refactor-technical
**SAD check:** with-sad | no-sad
**Date:**
**Author:** sad-architect-reviewer

> Warning block here when SAD_CHECK = no-sad:
> WARNING: produced without SAD validation.

## 1. Scope

Reference to .opencode/plans/refactor-analysis.md.

Summary of impacted modules, classes, files, endpoints, tables.

## 2. Non-regression contract

(Copied from refactor-analysis.md, kept synchronized.)

### 2.1 Preserved public APIs

| Method | Path | Request shape | Response shape | Status codes |
|---|---|---|---|---|

### 2.2 Preserved persistence schema entries

| Entry | Reason |
|---|---|

### 2.3 Preserved observable behaviors

| Behavior | Reason |
|---|---|

## 3. Characterization tests (safety net)

| # | Test name | Target | Assignee | Depends on |
|---|---|---|---|---|

## 4. Refactoring steps

| # | Pattern | Module | What changes | Why | How to verify | Rollback |
|---|---|---|---|---|---|---|

Each step row must reference a named refactoring pattern (Extract Method, Replace Conditional with Polymorphism, Strangler Fig, etc.).

## 5. Migration plan

### 5.1 Data migration

- Tooling:
- Forward script:
- Backward script:
- Backward-compatibility window:
- Cutover criteria:

### 5.2 API versioning

- Old version preserved until:
- New version exposed at:
- Deprecation plan:

### 5.3 Frontend impact

- Pages impacted:
- Coordination notes:

## 6. Risks and mitigations

| Risk | Likelihood | Impact | Mitigation | Rollback trigger |
|---|---|---|---|---|

## 7. Approval

- [ ] User approval received before /build refactor is executed.