# Refactor Analysis

**Trigger mode:** refactor-business | refactor-technical
**SAD check:** with-sad | no-sad
**Date:**
**Author:** technical-functional-analyst

> Warning block here when SAD_CHECK = no-sad:
> WARNING: produced without SAD validation.
> Findings are best-effort against generic architecture standards.

## 1. Trigger

### 1.1 Source

For refactor-business: cite knowledge/change-request.md and any related knowledge/ file.
For refactor-technical: state "Technical-debt assessment, no business change".

### 1.2 Summary

One paragraph describing what triggered the refactoring.

## 2. Impacted scope

### 2.1 Modules

| Module | Reason |
|---|---|

### 2.2 Classes and files

| Path | What changes |
|---|---|

### 2.3 Endpoints

| Method | Path | Change type (add / modify / remove / preserve) |
|---|---|---|

### 2.4 Persistence schema

| Table or column | Change type (add / modify / remove / preserve) |
|---|---|

## 3. Delta

### 3.1 Business rules (refactor-business only)

| Rule ID | State (new / modified / unchanged / removed) | Description |
|---|---|---|

### 3.2 Technical-debt items (refactor-technical only)

| Item | Evidence (file:line) | Severity | Proposed pattern |
|---|---|---|---|

## 4. Non-regression contract

### 4.1 Preserved public APIs

| Method | Path | Request shape | Response shape | Status codes |
|---|---|---|---|---|

### 4.2 Preserved persistence schema entries

| Entry | Reason it must be preserved |
|---|---|

### 4.3 Preserved observable behaviors

| Behavior | Reason it must be preserved |
|---|---|

## 5. Characterization tests required

| Test name | Target | Why required | Assignee |
|---|---|---|---|

## 6. Migration considerations

- Data migration: yes / no / TBD
- API versioning: yes / no / TBD
- Frontend impact: yes / no / TBD

Details:

## 7. Open questions

- Question 1 — for business / architect / security / legal
- Question 2 — ...

## 8. Readiness

- Readiness level: Ready for implementation | Ready with minor clarifications | Not ready — clarification required
- Main blockers:
- Recommended next actions: