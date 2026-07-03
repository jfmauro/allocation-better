# Extension Analysis

**Trigger mode:** extension-business
**SAD check:** with-sad | no-sad
**Date:**
**Author:** technical-functional-analyst

> Warning block here when SAD_CHECK = no-sad:
> WARNING: produced without SAD validation.
> Findings are best-effort against generic architecture standards.

## 1. Trigger

### 1.1 Source

Cite the new business analysis file(s) deposited under knowledge/.

### 1.2 Summary

One paragraph describing the new business scope.

### 1.3 Mode-switch recommendation

State explicitly whether the analysis confirms extension-business mode or recommends switching to refactor-business mode (with rationale).

## 2. New business scope

### 2.1 Epic

Epic title, business objective, business value, priority.

### 2.2 Features

| Feature | Description | Priority |
|---|---|---|

### 2.3 User stories

For each user story, produce the full per-story section as defined by the technical-analyst-builder template:

#### User Story [ID — Title]

##### 1. Context and Objective

##### 2. Detailed Functional Specifications

##### 3. API Contract

##### 4. Data Model

##### 5. Business Rules and Validations

##### 6. Error Management

##### 7. Edge Cases

##### 8. Dependencies

##### 9. Technical Acceptance Criteria

##### 10. UML Sequence Diagram

(Repeat per user story.)

## 3. Integration points with the existing codebase

### 3.1 Modules

| Module | Why touched | Action (add file / edit existing for wiring) |
|---|---|---|

### 3.2 Classes and files

| Path | Action | What is added |
|---|---|---|

### 3.3 Endpoints

| Existing controller path | New endpoints added | Notes |
|---|---|---|

### 3.4 Persistence schema

| Existing table | New columns / indexes | Additive? (yes / no — if no, document the decision) |
|---|---|---|

### 3.5 Configuration entries

| Configuration file | New entries | Rationale |
|---|---|---|

## 4. Preserved contract

### 4.1 Preserved public APIs

| Method | Path | Request shape | Response shape | Status codes |
|---|---|---|---|---|

### 4.2 Preserved persistence schema entries

| Entry | Reason it must be preserved |
|---|---|

### 4.3 Preserved observable behaviors

| Behavior | Reason it must be preserved |
|---|---|

## 5. Migration considerations

- Data migration: yes / no / TBD
- API versioning: yes / no / TBD
- Frontend impact: yes / no / TBD

Details:

## 6. Open questions

- Question 1 — for business / architect / security / legal
- Question 2 — ...

## 7. Readiness

- Readiness level: Ready for implementation | Ready with minor clarifications | Not ready — clarification required
- Main blockers:
- Recommended next actions:
- Mode-switch recommendation: Confirm extension-business | Switch to refactor-business (and rationale)