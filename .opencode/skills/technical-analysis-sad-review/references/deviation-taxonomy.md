# Deviation Taxonomy

## Finding Types

### SAD Gap

The target SAD lacks information required for reliable validation.

Examples:

- missing NFR;
- missing security section;
- missing data flow;
- missing error handling;
- unresolved TBD;
- diagram not available in text.

### Analysis Omission

The SAD contains the information, but the technical analysis does not address it.

Examples:

- API consumer omitted;
- external system omitted;
- retention rule omitted;
- audit requirement omitted;
- critical dependency omitted.

### Contradiction

The technical analysis states something incompatible with the target SAD.

Examples:

- analysis says local persistence exists while the SAD says no persistence;
- analysis uses direct database access while the SAD requires API-only access;
- analysis describes synchronous flow while the SAD defines asynchronous processing.

### Unsupported Addition

The analysis introduces information not present in the SAD and not justified by approved context.

Examples:

- new component;
- new technology;
- new database;
- unapproved architecture decision;
- unapproved scope extension.

### Quality-Standard Deviation

The analysis lacks content expected for a strong SAD-based review.

Examples:

- no NFR discussion;
- no security detail;
- no data ownership;
- no monitoring or exploitability;
- no risks.

### Non-Evaluable Item

The available information is insufficient to decide.

Examples:

- inaccessible diagram;
- missing interface contract;
- unresolved architecture decision;
- placeholder section.

## Severity Levels

### Blocking

Could cause wrong architecture, data loss, broken integration, security issue, regulatory non-compliance, or impossible operation.

### Major

Could cause rework, ambiguity, implementation risk, maintenance risk, or incomplete delivery.

### Minor

Improvement needed but not blocking.

### Information / Attention Point

Requires clarification or confirmation but is not necessarily a defect.