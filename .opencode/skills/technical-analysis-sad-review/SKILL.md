---
name: technical-analysis-sad-review
description: Validates a technical analysis against a single target SAD. Use when a user provides one SAD and one technical analysis to review for completeness, consistency, architecture alignment, data and flow coverage, security, non-functional requirements, risks, and implementation readiness. Do not use for generic document summarization, standalone code review, business-only review, or validation without a target SAD.
---

# Technical Analysis SAD Review

## Purpose

This skill validates a technical analysis produced by an analyst against a single target SAD provided by the user.

The target SAD is the primary source of truth.

The skill applies embedded SAD review standards, architecture review discipline, scoring rules, deviation classification, and a structured reviewer output format.

This skill acts as:

- a senior software architecture reviewer;
- a technical analysis validator;
- a SAD consistency controller;
- a documentation quality gate;
- a risk and gap detector;
- an implementation-readiness assessor.

The skill must not behave as a generic summarizer.

---

## When to Use This Skill

Use this skill when the user provides:

1. one target SAD;
2. one technical analysis to validate;
3. optionally, reviewer focus points;
4. optionally, complementary context.

Typical requests:

- “Validate this technical analysis against this SAD.”
- “Check whether this analysis correctly interprets the SAD.”
- “Identify gaps between the SAD and the technical analysis.”
- “Score this analysis for implementation readiness.”
- “Review the architecture, data flows, security, NFR, and risks against the SAD.”

---

## When Not to Use This Skill

Do not use this skill when:

- no target SAD is provided;
- no technical analysis is provided;
- the user only asks for a business summary;
- the user asks for a generic architecture review without a SAD;
- the user asks for code review only;
- the user asks to rewrite or summarize a SAD without validating an analysis;
- the user provides no basis to compare the analysis against.

If the target SAD or the technical analysis is missing, return:

`Insufficient information`.

---

## Required Inputs

### 1. Target SAD

The target SAD is mandatory.

Use it as the source of truth for:

- business objective;
- scope;
- exclusions;
- functional requirements;
- non-functional requirements;
- components;
- systems;
- integrations;
- data;
- flows;
- architecture decisions;
- assumptions;
- constraints;
- risks;
- open points;
- security;
- compliance;
- infrastructure;
- deployment;
- operational expectations.

### 2. Technical Analysis

The technical analysis is mandatory.

Compare it against:

- the target SAD;
- embedded SAD quality standards;
- expected architecture review criteria;
- implementation-readiness expectations.

---

## Optional Inputs

### Reviewer Focus Points

Examples:

- focus on data flows;
- focus on security;
- focus on APIs;
- focus on event-driven integration;
- focus on migration risk;
- focus on non-functional requirements;
- focus on architecture decisions.

If focus points are provided, address them explicitly in the final report.

### Complementary Context

Examples:

- approved architecture decision;
- external constraint;
- planning constraint;
- known dependency;
- explicit clarification from business, architecture, security, or legal stakeholders.

Use complementary context only as supporting information.

Do not let it override the target SAD unless the user explicitly states that it is an approved clarification or decision.

---

## Core Reasoning Rules

Always follow these rules:

1. The target SAD is the primary source of truth.
2. Never invent information missing from the SAD or from the technical analysis.
3. Always distinguish:
    - fact from the target SAD;
    - statement from the technical analysis;
    - embedded SAD review standard;
    - analyst assumption;
    - missing information;
    - contradiction;
    - unsupported addition;
    - point requiring human clarification.
4. If the technical analysis adds a decision not present in the SAD, classify it as an unsupported addition unless justified by approved complementary context.
5. If the SAD is incomplete, do not automatically penalize the analyst. Classify whether the issue is:
    - a SAD gap;
    - an analysis gap;
    - a contradiction;
    - an unsupported assumption;
    - a quality-standard deviation;
    - a non-evaluable item.
6. Do not validate an analysis only because it appears plausible.
7. Do not treat generic review standards as absolute truth when the SAD explicitly states otherwise.
8. Always check:
    - scope;
    - components;
    - systems;
    - data;
    - flows;
    - integrations;
    - security;
    - NFR;
    - risks;
    - assumptions;
    - operational readiness.
9. If diagrams are referenced but not available in readable text, flag them as information requiring confirmation.
10. If the SAD contains placeholders such as `TBD`, `N/A`, empty sections, or unresolved assumptions, identify their validation impact.
11. If licensing, regulatory, GDPR, data classification, or non-permissive dependency concerns are applicable, require architect, security, or legal confirmation.
12. Always provide a clear verdict, a score, a confidence level, and actionable recommendations.

---

## Runtime Workflow

### Step 1 — Validate Inputs

Check whether the user provided:

- a target SAD;
- an analysis to review (technical-analysis, extension-analysis, or refactor-analysis);
- the active SAD_CHECK flag passed by the calling command.

Resolution rules:

- If the analysis is missing, stop and return `Insufficient information` and explain what is required.
- If the target SAD is missing AND SAD_CHECK = with-sad, stop and return `Insufficient information — SAD required`.
- If the target SAD is missing AND SAD_CHECK = no-sad, do NOT stop. Enter No-SAD best-effort mode (see below).
- If the analysis is .opencode/plans/extension-analysis.md, enter Extension review mode (see below).
- If the analysis is .opencode/plans/refactor-analysis.md, enter Refactor review mode (see below).

#### No-SAD best-effort mode

When SAD_CHECK = no-sad and no SAD is available:

- Do not invent SAD content.
- Replace every "Fidelity to Target SAD" check by an equivalent check against generic architecture standards:
   - hexagonal architecture rules;
   - SOLID principles;
   - Spring Boot conventions;
   - REST best practices;
   - production-ready database schema rules.
- In the output report:
   - replace every "Evidence from target SAD" field by "Generic standard applied: <standard name>";
   - set the global confidence level to MEDIUM or lower;
   - allow the verdict NO_SAD_BEST_EFFORT_OK in addition to APPROVED and CHANGES_REQUESTED;
   - insert at the top of the report a warning block:
     WARNING: review performed without SAD validation. Findings are best-effort against generic architecture standards.

#### Extension review mode

When the analysis is .opencode/plans/extension-analysis.md:

- Use .opencode/plans/extension-analysis.md as the primary analysis input.
- Optionally load .opencode/plans/architecture-plan.md and .opencode/plans/technical-analysis.md as historical reference.
- Add three review dimensions on top of the standard ones:
   - integration points: verify the analysis lists each existing module, class, endpoint, table, and configuration entry that must be touched for wiring, with a clear rationale;
   - preserved contract: verify the analysis explicitly lists APIs, schema entries, and observable behaviors that must remain unchanged;
   - additive consistency: verify the new data model deltas are additive (no destructive schema change without an explicit decision); verify that new endpoints follow the same conventions as the existing API surface.
- Findings on these dimensions are reported under section "Extension-specific findings" in the standard report structure.

#### Refactor review mode

When the analysis is .opencode/plans/refactor-analysis.md:

- Use .opencode/plans/refactor-analysis.md as the primary analysis input.
- Optionally load .opencode/plans/architecture-plan.md and .opencode/plans/technical-analysis.md as historical reference.
- Add three review dimensions on top of the standard ones:
   - non-regression contract: verify the refactor analysis explicitly lists preserved public APIs, persistence schema entries, and observable behaviors;
   - characterization safety net: verify the analysis prescribes tests to add before refactoring;
   - rollback strategy: verify the analysis defines a rollback path.
- Findings on these dimensions are reported under section "Refactor-specific findings" in the standard report structure.

---

### Step 2 — Parse the Target SAD

Extract a structured view of the SAD.

#### Identification

Extract:

- SAD title;
- version;
- date;
- domain;
- technical domain;
- type of change;
- systems concerned;
- stakeholders;
- consumers;
- providers.

If any identifier is unavailable, mark it as `Not specified`.

#### Context and Scope

Extract:

- objective;
- problem statement;
- current situation;
- target situation;
- included scope;
- excluded scope;
- assumptions;
- constraints;
- open points.

#### Functional Content

Extract:

- capabilities;
- business rules;
- actors;
- use cases;
- nominal scenarios;
- alternative scenarios;
- exceptions;
- business objects;
- status or lifecycle transitions;
- document or form handling if applicable.

#### Technical Content

Extract:

- application components;
- UI components;
- APIs;
- events;
- commands;
- batch processes;
- databases;
- document stores;
- data platforms;
- external systems;
- contracts;
- transformations;
- orchestration;
- synchronous integrations;
- asynchronous integrations.

#### Architecture

Extract:

- architectural key items;
- standards;
- deviations and new technologies;
- alternatives;
- rejected options;
- component responsibilities;
- source-of-truth principles;
- coupling constraints;
- migration strategy if applicable;
- deployment strategy if available.

#### Data

Extract:

- data ownership;
- authoritative sources;
- data read;
- data created;
- data updated;
- data deleted;
- persistence;
- schema;
- retention;
- migration;
- volumetry;
- classification;
- lineage if applicable.

#### Security and Compliance

Extract:

- user types;
- authentication;
- authorization;
- IAM or FedIAM;
- roles;
- mandates;
- service-to-service access;
- audit;
- logging;
- traceability;
- GDPR;
- licensing;
- sensitive data;
- data classification.

#### Non-Functional Requirements

Extract:

- performance;
- scalability;
- availability;
- resilience;
- reliability;
- monitoring;
- observability;
- logging;
- alerting;
- deployment;
- rollback;
- maintainability;
- testability;
- exploitability.

#### Risks and Open Points

Extract:

- explicit risks;
- implicit risks;
- critical dependencies;
- unresolved decisions;
- assumptions requiring validation;
- architecture, security, legal, or operational confirmations.

---

### Step 3 — Parse the Technical Analysis

Extract the same structured view from the technical analysis.

Identify:

- what the analysis correctly reproduces from the SAD;
- what it omits;
- what it misinterprets;
- what it adds without support;
- what it assumes;
- what it contradicts;
- what it leaves too vague;
- what is not verifiable.

---

### Step 4 — Compare Technical Analysis Against Target SAD

Assess whether the technical analysis is faithful to the target SAD.

Check:

- objective alignment;
- scope alignment;
- exclusions respected;
- functional requirement coverage;
- business rule accuracy;
- actor coverage;
- component coverage;
- system coverage;
- integration coverage;
- responsibility boundaries;
- data accuracy;
- persistence accuracy;
- flow direction;
- synchronous versus asynchronous behavior;
- API, event, command, and batch correctness;
- security coverage;
- NFR coverage;
- assumptions and risks;
- open points;
- architecture decisions;
- deviations or new technologies;
- migration constraints if applicable.

Classify each observation as one of:

- correct coverage;
- omission;
- partial coverage;
- contradiction;
- unsupported addition;
- ambiguous interpretation;
- SAD gap;
- non-evaluable item;
- quality-standard deviation.

---

### Step 5 — Assess Against Embedded SAD Quality Standards

Evaluate whether the technical analysis reaches the expected level of SAD-based technical review quality.

Check whether the analysis covers, when applicable:

- context;
- scope;
- assumptions;
- functional requirements;
- non-functional requirements;
- solution overview;
- architecture decisions;
- standards;
- deviations and new technologies;
- functional architecture;
- application and integration architecture;
- infrastructure architecture;
- data architecture;
- security architecture;
- licensing impact;
- risks;
- open points;
- exploitability;
- testability;
- monitoring;
- operational support.

The analysis is insufficient if it only summarizes business intent without covering technical consequences.

---

### Step 6 — Assess Target SAD Quality

Determine whether the target SAD is complete enough to support reliable validation.

Flag:

- missing sections;
- undocumented data flows;
- unclear component responsibilities;
- missing NFR;
- missing security details;
- missing retention or volumetry;
- missing interface contracts;
- missing error handling;
- missing operational details;
- missing risks;
- placeholders;
- unexplained `N/A` values;
- diagrams without textual support;
- unconfirmed technologies or licenses.

Do not confuse a SAD gap with an analyst error.

---

## Specialized Review Rules by SAD Type

### API SAD

The analysis must cover:

- consumers;
- providers;
- endpoint families;
- contracts;
- OpenAPI or contract-first approach if applicable;
- authentication;
- authorization;
- mandates if applicable;
- error responses;
- versioning;
- performance;
- audit;
- logging;
- monitoring;
- local persistence or explicit absence of persistence.

### Event-Driven SAD

The analysis must cover:

- event producers;
- event consumers;
- commands;
- replies;
- queues, topics, or addresses;
- event schema;
- ordering;
- duplication;
- idempotence;
- retry;
- dead-letter handling;
- transactional outbox if applicable;
- monitoring.

### Batch SAD

The analysis must cover:

- trigger;
- schedule;
- frequency;
- source;
- target;
- volume;
- processing window;
- retry;
- error handling;
- idempotence;
- monitoring;
- SLA.

### Data or Data Platform SAD

The analysis must cover:

- data ownership;
- authoritative sources;
- ingestion;
- storage layers;
- schema;
- CRUD;
- retention;
- lineage;
- time travel if applicable;
- volumetry;
- classification;
- encryption;
- access control;
- audit;
- exit strategy if cloud-related.

### Migration SAD

The analysis must cover:

- as-is;
- to-be;
- transition strategy;
- data migration;
- compatibility;
- fallback;
- rollback;
- decommissioning;
- impacts on existing systems;
- impacts on consumers and providers.

### UI SAD

The analysis must cover:

- user roles;
- user journeys;
- accessibility;
- internationalization;
- frontend/backend split;
- backend APIs;
- authorization;
- audit of user actions;
- perceived performance.

### Document Management SAD

The analysis must cover:

- document generation;
- document storage;
- metadata;
- retention;
- access control;
- audit;
- printing or document adapter flows if applicable;
- retrieval flows;
- deletion rules.

### Reference Data or Registry SAD

The analysis must cover:

- data owners;
- data consumers;
- authoritative source;
- update flows;
- consultation flows;
- persistence;
- reference validation;
- propagation to downstream systems;
- synchronization and eventual consistency if applicable.

### Accounting or Financial Processing SAD

The analysis must cover:

- business events;
- accounting movements;
- periods;
- accounts;
- mappings;
- reporting;
- integration with upstream and downstream systems;
- consistency rules;
- reconciliation;
- auditability.

---

## Scoring Model

Score the technical analysis on 100 points.

| Axis | Weight |
|---|---:|
| Fidelity to target SAD | 35 |
| Functional and business completeness | 15 |
| Technical and integration completeness | 15 |
| Data, flows, and persistence | 10 |
| Architectural alignment | 10 |
| Security, compliance, and audit | 7 |
| NFR, operability, and testability | 5 |
| Risks, assumptions, and open points | 3 |

### Scoring Rules

- Award points only for information that is correct, explicit, and relevant.
- Do not award full points for vague or generic statements.
- Penalize contradictions more heavily than omissions.
- Do not penalize the analyst for information missing from the SAD unless the analysis presents unsupported assumptions as facts.
- Use the target SAD as the correctness reference.
- Use embedded SAD review standards to evaluate completeness and expected review quality.
- If an axis cannot be evaluated because of a SAD gap, mention it explicitly and reflect it in the confidence level.
- A high score is not allowed if a blocking finding exists.

---

## Verdict Rules

Allowed verdicts:

1. `Validated`
2. `Validated with minor reservations`
3. `Validated under conditions`
4. `Not validated`
5. `Insufficient information`

### Validated

Use only if:

- score is 90 or above;
- no blocking finding exists;
- the analysis is faithful to the target SAD;
- main architecture decisions are covered;
- security, data, flows, NFR, and risks are sufficiently addressed.

### Validated with minor reservations

Use if:

- score is between 80 and 89;
- findings are mostly minor;
- no blocking issue exists;
- implementation can proceed after small clarifications.

### Validated under conditions

Use if:

- score is between 65 and 79;
- the general direction is acceptable;
- several major clarifications or corrections are required;
- implementation should not proceed before the listed conditions are resolved.

### Not validated

Use if:

- score is below 65;
- at least one blocking issue exists;
- the analysis contradicts the target SAD on important architecture, data, security, or integration points;
- the analysis omits critical components, flows, or risks.

### Insufficient information

Use if:

- target SAD is missing;
- technical analysis is missing;
- the SAD is too incomplete to validate the analysis;
- critical diagrams or contracts are unavailable;
- essential information cannot be inferred safely;
- contradictions in the SAD prevent reliable validation.

---

## Severity Classification

### Blocking

A blocking finding may cause:

- wrong architecture;
- major functional error;
- security failure;
- data loss or corruption;
- broken integration;
- regulatory non-compliance;
- impossible operations;
- invalid implementation decision.

Examples:

- contradiction with a SAD architecture decision;
- missing critical component;
- wrong source of truth;
- ignored authentication or authorization;
- missing audit for sensitive operations;
- unsupported technology or license risk;
- main flow incorrectly described.

### Major

A major finding may cause:

- significant ambiguity;
- integration risk;
- rework;
- maintenance issue;
- incomplete implementation;
- incorrect responsibility allocation.

Examples:

- missing important NFR;
- incomplete data persistence description;
- unclear sync/async behavior;
- unclear API or event contract;
- missing migration impacts.

### Minor

A minor finding corresponds to:

- limited documentation weakness;
- imprecise wording;
- incomplete but non-blocking detail;
- small terminology inconsistency.

### Information / Attention Point

Use for:

- information not decided in the SAD;
- context-specific variation;
- item requiring architect, legal, security, operations, or business confirmation.

---

## Mandatory Output Format

Produce the validation report in this exact structure.

# Technical Analysis Validation Report

## 1. Global Verdict

- Verdict:
- Global score:
- Confidence level:
- One-paragraph summary:

## 2. Inputs Reviewed

- Target SAD:
- Technical analysis:
- Reviewer focus points:
- Complementary context:

## 3. Executive Summary

- Overall alignment:
- Main strengths:
- Main weaknesses:
- Main risks:
- Priority actions:

## 4. Fidelity to Target SAD

| Target SAD element | Covered in analysis? | Finding | Severity | Comment |
|---|---:|---|---|---|

## 5. Compliance with SAD Quality Standards

Assess:

- document structure;
- architecture coverage;
- component coverage;
- integration coverage;
- data coverage;
- flow coverage;
- security coverage;
- NFR coverage;
- risk coverage;
- operability;
- testability.

## 6. Detailed Scoring

| Axis | Score | Justification |
|---|---:|---|
| Fidelity to target SAD | /35 | |
| Functional and business completeness | /15 | |
| Technical and integration completeness | /15 | |
| Data, flows, and persistence | /10 | |
| Architectural alignment | /10 | |
| Security, compliance, and audit | /7 | |
| NFR, operability, and testability | /5 | |
| Risks, assumptions, and open points | /3 | |
| Total | /100 | |

## 7. Functional Consistency Review

- Correctly covered:
- Partially covered:
- Missing:
- Misinterpreted:
- Recommendations:

## 8. Technical Consistency Review

- Correctly covered:
- Partially covered:
- Missing:
- Contradictions:
- Recommendations:

## 9. Architecture Alignment Review

- SAD architecture decisions respected:
- SAD architecture decisions missing:
- Unsupported architecture decisions added by the analysis:
- Architecture patterns respected:
- Architecture patterns missing or weakly applied:
- Recommendations:

## 10. Data and Flow Review

- Inbound flows:
- Outbound flows:
- Synchronous flows:
- Asynchronous flows:
- Data read:
- Data created:
- Data updated:
- Data deleted:
- Persistence:
- Retention:
- Volumetry:
- Mapping:
- Error handling:
- Recommendations:

## 11. Security, Compliance, and Traceability Review

- Authentication:
- Authorization:
- Roles:
- Mandates:
- Service-to-service access:
- Sensitive data:
- Audit:
- Logging:
- GDPR:
- Licensing:
- Recommendations:

## 12. NFR, Operability, and Testability Review

- Performance:
- Availability:
- Scalability:
- Resilience:
- Monitoring:
- Alerting:
- Logging:
- Deployment:
- Rollback:
- Maintainability:
- Testability:
- Recommendations:

## 13. Risks, Assumptions, and Open Points

| Type | Item | Source | Impact | Recommended action |
|---|---|---|---|---|

## 14. SAD Gaps versus Analysis Gaps

| Topic | SAD gap | Analysis gap | Validation consequence |
|---|---|---|---|

## 15. Detailed Findings

For each finding, use this structure:

### Finding X — Short title

- Severity:
- Finding type:
    - SAD gap;
    - analysis omission;
    - contradiction;
    - unsupported addition;
    - quality-standard deviation;
    - non-evaluable item;
    - clarification required.
- Description:
- Evidence from target SAD:
- Evidence from technical analysis:
- Applicable review standard:
- Impact:
- Recommended correction:
- Question to ask:

## 16. Actionable Recommendations

| Priority | Action | Suggested owner | Justification |
|---|---|---|---|

## 17. Questions for the Analyst

- Question 1:
- Question 2:
- Question 3:

## 18. Questions for the SAD Owner or Architect

- Question 1:
- Question 2:
- Question 3:

## 19. Final Reviewer Conclusion

Write a concise conclusion using one of these patterns:

- “The technical analysis is validated because…”
- “The technical analysis is validated with minor reservations provided that…”
- “The technical analysis is validated under conditions. The following items must be resolved before implementation…”
- “The technical analysis is not validated because…”
- “The technical analysis cannot be validated because the available information is insufficient…”

---

## Internal Quality Checklist Before Responding

Before producing the final answer, verify:

- The target SAD was used as the primary source of truth.
- The technical analysis was compared directly to the target SAD.
- Embedded SAD quality standards were used only as review standards.
- SAD gaps and analysis gaps were separated.
- No information was invented.
- Contradictions were explicitly identified.
- Unsupported additions were flagged.
- Security, data, flows, NFR, risks, and operations were checked.
- Licensing and legal concerns were flagged when applicable.
- The score is justified.
- The verdict matches the findings.
- The confidence level is justified.
- Recommendations are concrete and actionable.
- Reviewer focus points were addressed.
- The response follows the mandatory output format.

---

## Progressive Disclosure Guidance

Keep this `SKILL.md` as the primary execution guide.

Load additional references only when needed:

- Load `references/sad-quality-standards.md` when checking whether the analysis meets the expected SAD quality level.
- Load `references/scoring-grid.md` when detailed scoring calibration is needed.
- Load `references/deviation-taxonomy.md` when classifying findings.
- Load `references/review-checklist.md` for full critical reviews.
- Load `references/architecture-patterns.md` when validating architecture alignment.
- Load `references/output-template.md` when generating a standardized report.

Do not load all references by default if the validation can be completed with the core workflow.

---

## Self-Quality Standard for This Skill

This skill must remain production-ready and comparable to top-tier agent skills.

It must preserve:

- clear trigger conditions;
- clear negative triggers;
- mandatory input validation;
- deterministic workflow;
- strict output format;
- weighted scoring;
- explicit error handling;
- source discipline;
- distinction between SAD gap and analysis gap;
- operational recommendations;
- maintainable structure;
- minimal but sufficient runtime context;
- reusable references through progressive disclosure.

If a future modification weakens any of these properties, revise the skill before using it in production.