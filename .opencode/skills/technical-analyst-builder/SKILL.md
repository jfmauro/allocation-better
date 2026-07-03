---
name: technical-analyst-builder
description: Builds a detailed functional and technical analysis from an existing business analysis and a target SAD. Use when the user provides a business analysis, a SAD, and expects a complete implementation-ready analysis following a fixed template with user-story-level functional details, API contracts, data models, business rules, error handling, edge cases, dependencies, tests, and PlantUML sequence diagrams. Do not use for generic SAD review, standalone code review, pure business summarization, or validation without source business analysis.
---

# Technical Analyst Builder

## Purpose

This skill creates a precise functional and technical analysis from:

1. a business analysis;
2. a target SAD;
3. a fixed functional and technical analysis template.

The business analysis defines the functional scope and business intent.

The SAD defines architectural constraints, technologies, integrations, security, data, non-functional requirements, infrastructure expectations, and implementation boundaries.

The fixed template defines the mandatory output structure.

This skill must produce an implementation-ready analysis that developers can use without ambiguity.

It must not behave as a generic summarizer.

---

## Primary Mission

Generate a professional functional and technical analysis that:

- implements the business analysis as the priority;
- remains aligned with the target SAD;
- respects the fixed analysis template;
- is structured by Epic, Feature, User Story, and Technical Analysis;
- provides enough detail for implementation;
- separates functional rules from technical decisions;
- explicitly identifies assumptions, SAD gaps, business-analysis gaps, and questions;
- avoids inventing unsupported requirements or architecture decisions.

---

## When to Use This Skill

Use this skill when the user asks to:

- create a technical analysis from a business analysis and a SAD;
- transform user stories into implementable functional and technical specifications;
- produce API contracts, data models, validation rules, edge cases, and tests;
- enrich a business analysis with SAD-compliant technical design;
- generate developer-ready analysis sections using a fixed template;
- prepare an analysis before implementation.

Typical requests:

- “Create the technical analysis from this business analysis and this SAD.”
- “Write the functional and technical analysis using the standard template.”
- “Transform these user stories into implementable technical specifications.”
- “Use the SAD to complete the technologies, NFR, dependencies and architecture constraints.”
- “Generate the analysis per user story.”

---

## When Not to Use This Skill

Do not use this skill when:

- no business analysis is provided;
- no SAD is provided;
- the user only asks for a generic business summary;
- the user asks for code implementation;
- the user asks for code review only;
- the user asks to validate an already written technical analysis without generating one;
- the user asks for a SAD review without creating a technical-functional analysis.

If the business analysis is missing, return:

`Insufficient information — business analysis is required.`

If the SAD is missing, return:

`Insufficient information — target SAD is required.`

If the fixed template is missing, use the embedded standard template structure defined in this skill.

---

## Required Inputs

### 1. Business Analysis

Mandatory.

Use it as the primary source for:

- epic;
- features;
- user stories;
- business objectives;
- business context;
- business rules;
- actors;
- input data;
- acceptance criteria;
- workflows;
- statuses;
- expected outcomes;
- edge cases;
- business priorities;
- end-to-end process;
- minimal domain model.

The business analysis defines what must be implemented.

### 2. Target SAD

Mandatory.

Use it as the architectural and technical source for:

- architecture decisions;
- application components;
- integration patterns;
- technologies;
- databases;
- infrastructure;
- APIs;
- events;
- batch processes;
- security;
- IAM/FedIAM or equivalent identity integration;
- audit;
- data classification;
- NFR;
- monitoring;
- logging;
- deployment;
- constraints;
- deviations;
- dependencies.

The SAD defines how the solution must fit into the architecture.

### 3. Fixed Analysis Template

Optional if embedded template is available.

Use it to enforce the mandatory document structure.

---

## Optional Inputs

### Reviewer or Architect Focus Points

Examples:

- focus on API contracts;
- focus on data model;
- focus on concurrency and idempotency;
- focus on security;
- focus on UI validation;
- focus on integration with existing components;
- focus on NFR and monitoring.

### Complementary Context

Examples:

- approved architecture clarification;
- business decision;
- technical constraint;
- naming convention;
- existing endpoint convention;
- database naming rules;
- implementation stack.

Complementary context may enrich the output but must not silently override the business analysis or SAD.

---

## Source Priority Rules

Use this source priority model:

1. Business analysis defines functional intent and user-story scope.
2. SAD defines architecture, technology, integration, NFR, security, and operational constraints.
3. Fixed template defines output structure.
4. Complementary context may clarify, but only if explicitly approved.
5. If sources conflict, do not guess. Flag the conflict and propose a resolution question.

### Conflict Handling

If the business analysis and SAD conflict:

- do not silently choose one;
- preserve the business requirement;
- preserve the SAD constraint;
- explain the conflict;
- mark the affected section as `Requires clarification`;
- ask a concrete question to the business analyst or architect.

Examples:

- business analysis requires a new API, SAD only allows event-driven integration;
- business analysis implies synchronous allocation, SAD requires asynchronous processing;
- business analysis requires sensitive data display, SAD mandates masking;
- business analysis defines a functional rule not covered by SAD security constraints.

---

## Non-Invention Rules

Never invent:

- systems;
- APIs;
- database tables;
- fields;
- roles;
- permissions;
- technologies;
- NFR values;
- infrastructure;
- external dependencies;
- architecture decisions;
- legal or security rules.

If needed information is missing, write:

- `Not specified in the business analysis`;
- `Not specified in the SAD`;
- `Requires business clarification`;
- `Requires architect confirmation`;
- `Requires security/legal confirmation`.

You may propose reasonable implementation options only if clearly labelled as:

`Proposed option — requires confirmation`.

---

## Runtime Workflow

### Step 1 — Validate Inputs

Check that the following are available:

- a business analysis (any file under knowledge/baseline/, knowledge/inbox/, or the root of knowledge/ that describes business intent);
- a target SAD (filename containing "sad" case-insensitive, in any of the above locations);
- the active mode and SAD_CHECK flag passed by the calling command.

Knowledge directory convention:

- knowledge/baseline/ - historical context;
- knowledge/inbox/ - new scope to process during the current cycle;
- Files at the root of knowledge/ - treated as baseline by default with a backward-compatibility warning.

Resolution rules:

- If the business analysis is missing in all locations, stop and return `Insufficient information`.
- If the target SAD is missing AND SAD_CHECK = with-sad, stop and return `Insufficient information — SAD required`.
- If the target SAD is missing AND SAD_CHECK = no-sad, do NOT stop. Enter No-SAD best-effort mode (see below).
- If the fixed template is not provided, apply the embedded template structure.
- If the active mode is extension-business or refactor-business, also load the contents of knowledge/inbox/ as the authoritative new scope. Abort if inbox/ is empty.
- If the active mode is refactor-technical, knowledge/inbox/ may be empty.
- If the active mode is extension-business, refactor-business or refactor-technical, also load the existing codebase (read-only) and the historical plan files under .opencode/plans/ as additional inputs.

#### No-SAD best-effort mode

When SAD_CHECK = no-sad and no SAD is available:

- Do not invent SAD content.
- Replace SAD-derived constraints by generic architecture standards: hexagonal architecture rules, SOLID, Spring Boot conventions, REST best practices.
- Every section that would normally cite the SAD must instead state: "Requires architect confirmation (no SAD available)".
- Add at the top of the produced analysis a warning block:
  WARNING: produced without SAD validation.
  Findings are best-effort against generic architecture standards.
- All other workflow steps remain identical.

#### Extension mode

When the active mode is extension-business:

- Read the existing codebase (read-only) before producing any output.
- Read .opencode/plans/architecture-plan.md and .opencode/plans/technical-analysis.md if present, as historical baseline.
- The contents of knowledge/inbox/ ARE the new scope, by convention.
- Produce .opencode/plans/extension-analysis.md (not technical-analysis.md) following the template at .opencode/templates/extension-analysis.md.
- Per user story, apply the full mandatory output structure of this skill.
- In addition, produce two transversal sections:
    - "Integration points with the existing codebase";
    - "Preserved contract".
- In the Readiness Assessment, if the new business analysis primarily modifies existing behavior (more than 30 percent of the user stories impact already-implemented features), recommend switching to refactor-business mode and stop the analysis.
- Load the feature-extension-methodology skill in addition to this one.

#### Refactoring mode

When the active mode is refactor-business or refactor-technical:

- Read the existing codebase (read-only) before producing any output.
- Read .opencode/plans/architecture-plan.md and .opencode/plans/technical-analysis.md if present, as historical reference.
- Produce .opencode/plans/refactor-analysis.md (not technical-analysis.md) following the template at .opencode/templates/refactor-analysis.md.
- For refactor-business: the change request lives in knowledge/inbox/ (any filename); the SAD (when available) defines the architectural constraints; the existing codebase defines the baseline.
- For refactor-technical: the existing codebase is the primary input; no business change is allowed.
- Load the refactoring-methodology skill in addition to this one.

---

### Step 2 — Parse the Business Analysis

Extract:

- epic title and objective;
- business context;
- core business rules;
- user stories;
- acceptance criteria;
- actors;
- input data;
- output data;
- statuses;
- workflows;
- validation rules;
- business exceptions;
- edge cases;
- end-to-end scenarios;
- minimal data model;
- API suggestions if provided;
- NFR explicitly defined in the business analysis.

For each user story, identify:

- user story ID;
- title;
- actor;
- intent;
- business value;
- priority if available;
- preconditions;
- trigger;
- nominal flow;
- alternative flows;
- business rules;
- acceptance criteria;
- data touched;
- UI impact;
- API impact;
- persistence impact;
- security impact;
- NFR impact;
- dependencies;
- test cases.

---

### Step 3 — Parse the SAD

Extract:

- solution context;
- target architecture;
- components;
- bounded contexts or domains;
- integration architecture;
- technologies;
- API standards;
- event or messaging standards;
- database standards;
- document storage if applicable;
- infrastructure;
- deployment model;
- monitoring;
- logging;
- audit;
- security model;
- authentication;
- authorization;
- roles;
- data classification;
- GDPR or privacy constraints;
- NFR;
- performance requirements;
- availability requirements;
- scalability requirements;
- resilience requirements;
- known constraints;
- deviations;
- open points.

---

### Step 4 — Map Business Requirements to SAD Architecture

For each user story:

1. Identify the SAD components involved.
2. Identify whether the story requires:
   - UI;
   - API;
   - event;
   - command;
   - batch;
   - database changes;
   - external service call;
   - audit event;
   - security check;
   - NFR consideration.
3. Determine whether the SAD already defines the implementation path.
4. Identify gaps between business requirements and SAD information.
5. Mark unresolved mapping issues as clarification points.

---

### Step 5 — Generate the Analysis Per User Story

For each user story, produce a complete and autonomous technical-functional analysis using the mandatory template.

Each user story section must allow a developer to implement the story without ambiguity.

Do not merge unrelated user stories unless the user explicitly asks for a consolidated analysis.

---

## Mandatory Output Structure

Always structure the output as:

# Functional and Technical Analysis

## Epic: [Epic Title]

### Feature: [Feature Name]

#### User Story: [US-ID — Title]

##### 1. Context and Objective

Include:

- User Story reference;
- ID;
- title;
- priority;
- actor;
- “I want” statement;
- “So that” statement;
- technical objective;
- technical prerequisites;
- source references:
   - business analysis section;
   - SAD section if available.

##### 2. Detailed Functional Specifications

Include:

- user or system flow;
- screen/interface specification if applicable;
- UI components if applicable;
- validation rules;
- draft/state management if applicable;
- business statuses;
- manual versus automatic behavior;
- business constraints;
- authorization-dependent behavior.

If there is no UI, explicitly write:

`No user interface is required for this user story.`

If the story creates or updates an HTML screen, describe:

- screen title;
- displayed fields;
- masked fields;
- actions;
- buttons;
- validation messages;
- permission checks;
- audit triggers.

##### 3. API Contract

For each endpoint, document:

- endpoint purpose;
- HTTP method;
- path;
- headers;
- path parameters;
- query parameters;
- request body;
- request constraints;
- successful responses;
- error responses;
- idempotency behavior;
- authentication and authorization;
- audit behavior;
- related business rules.

If the SAD requires contract-first or a specific API standard, respect it.

If no API is required, write:

`No API endpoint is required for this user story.`

##### 4. Data Model

Document:

- tables or aggregates;
- fields;
- types if known;
- constraints;
- indexes;
- uniqueness rules;
- relationships;
- CRUD impact;
- source of truth;
- retention if known;
- audit fields;
- migration impact if applicable.

If exact physical table names are unknown, use logical names and mark them as requiring confirmation.

##### 5. Business Rules and Validations

Document every relevant rule with:

- rule ID;
- name;
- description;
- conditions;
- normalization;
- validation algorithm;
- failure behavior;
- examples if useful.

Business rules from the business analysis must be preserved.

Technology or architecture constraints from the SAD must be added where applicable.

##### 6. Error Management

Document:

- HTTP status if applicable;
- API error code;
- user-facing message;
- corrective action;
- retry strategy;
- logging level;
- audit event if applicable.

Include business errors and technical errors.

##### 7. Edge Cases

Include:

- validation edge cases;
- security edge cases;
- concurrency edge cases;
- idempotency edge cases;
- partial failure cases;
- duplicate processing;
- ambiguous matches;
- missing data;
- external dependency failure;
- rollback behavior.

Concurrency-sensitive user stories must explicitly describe atomicity and locking expectations.

##### 8. Dependencies

Document:

- technical dependencies;
- functional dependencies;
- external services;
- upstream systems;
- downstream systems;
- SAD components;
- permissions;
- configuration parameters;
- feature toggles if applicable.

##### 9. Technical Acceptance Criteria

Include:

- integration tests;
- API tests;
- data persistence tests;
- business-rule tests;
- security tests;
- performance tests where applicable;
- concurrency tests where applicable;
- audit tests;
- negative tests.

Each test must have:

- test name;
- endpoint or component;
- expected result;
- main assertion.

##### 10. UML Sequence Diagram

Provide PlantUML only.

Include:

- nominal flow;
- main alternative or error flow.

The diagram must be consistent with the generated technical analysis.

---

## Required Writing Style

Use:

- clear professional English;
- concise but complete specifications;
- deterministic wording;
- explicit requirements;
- structured tables where useful;
- JSON examples where they add value;
- PlantUML for sequence diagrams;
- developer-oriented precision.

Avoid:

- vague statements;
- generic architecture comments;
- unsupported assumptions;
- hidden decisions;
- excessive prose;
- implementation code unless requested;
- contradictions between sections.

---

## Template Compliance Rules

The generated analysis must respect the fixed template structure.

Each user story must include all ten mandatory sections:

1. Context and Objective
2. Detailed Functional Specifications
3. API Contract
4. Data Model
5. Business Rules and Validations
6. Error Management
7. Edge Cases
8. Dependencies
9. Technical Acceptance Criteria
10. UML Sequence Diagram

If a section is not applicable, include it anyway and state why it is not applicable.

Never omit a mandatory section.

---

## SAD Alignment Rules

When using the SAD:

- use SAD technologies only if explicitly specified;
- use SAD integrations only if relevant to the user story;
- preserve SAD architecture decisions;
- preserve SAD security constraints;
- preserve SAD NFR;
- preserve SAD data ownership rules;
- preserve SAD interface standards;
- preserve SAD deployment and monitoring expectations.

If the business analysis requires something not covered by the SAD, mark it as:

`SAD gap — requires architect confirmation`.

---

## Business Analysis Preservation Rules

When using the business analysis:

- preserve all user stories;
- preserve all acceptance criteria;
- preserve all business rules;
- preserve the priority order of flows;
- preserve mandatory manual validation rules;
- preserve status transitions;
- preserve validation algorithms;
- preserve audit requirements;
- preserve edge cases;
- preserve end-to-end scenarios.

Do not weaken or reinterpret a business rule unless the SAD explicitly constrains it.

---

## Quality Gate Before Final Output

Before producing the final response, verify:

- every user story from the business analysis is covered;
- every acceptance criterion is mapped to functional and/or technical specifications;
- the SAD constraints are reflected where applicable;
- all mandatory template sections are present;
- API contracts are consistent with the flows;
- data model supports the business rules;
- status transitions are coherent;
- errors and edge cases are documented;
- security and privacy are covered;
- NFR and operability are covered;
- auditability is covered;
- concurrency and idempotency are covered where applicable;
- PlantUML diagrams are syntactically plausible;
- assumptions and open questions are explicit.

If the quality gate fails, revise the output before answering.

---

## Final Output Footer

At the end of the generated analysis, include:

## Open Questions and Clarifications

Group questions by:

- Business clarification;
- Architecture clarification;
- Security clarification;
- Data clarification;
- Operational clarification.

## Traceability Matrix

Provide a matrix mapping:

- business user story;
- acceptance criteria;
- generated analysis section;
- SAD dependency;
- implementation impact.

## Readiness Assessment

Provide:

- readiness level:
   - Ready for implementation;
   - Ready with minor clarifications;
   - Not ready — clarification required.
- main blockers;
- recommended next actions.

---

## Progressive Disclosure Guidance

Use this SKILL.md as the core workflow.

Load additional references only when needed:

- `references/analysis-template.md` when the full fixed template is required.
- `references/sad-extraction-guide.md` when the SAD is long or complex.
- `references/business-analysis-extraction-guide.md` when the business analysis contains many user stories.
- `references/conflict-resolution-rules.md` when business analysis and SAD conflict.
- `references/quality-checklist.md` before final output.
- `references/output-contract.md` when a strict deliverable format is required.

Do not load every reference by default.

---

## Self-Quality Standard

This skill must remain production-ready and comparable to top-tier agent skills.

It must preserve:

- clear trigger conditions;
- clear negative triggers;
- mandatory input validation;
- deterministic workflow;
- strict output structure;
- source-priority rules;
- conflict handling;
- non-invention discipline;
- template compliance;
- SAD alignment;
- business traceability;
- implementation readiness;
- progressive disclosure;
- maintainable structure.

If a future modification weakens any of these properties, revise the skill before using it.