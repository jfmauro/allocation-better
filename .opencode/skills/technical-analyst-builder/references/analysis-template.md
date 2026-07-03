# Fixed Functional and Technical Analysis Template

The generated analysis must follow this hierarchy:

- EPIC
- FEATURE
- USER STORY
- TECHNICAL ANALYSIS

Each user story must contain a complete, standalone analysis.

## Mandatory Sections Per User Story

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

## Section Requirements

### 1. Context and Objective

Must include:

- user story ID;
- title;
- priority;
- actor;
- business intent;
- business value;
- technical objective;
- technical prerequisites.

### 2. Detailed Functional Specifications

Must include:

- user/system flow;
- UI or interface description if applicable;
- validation rules;
- screen behavior if applicable;
- status changes;
- permission-dependent actions.

### 3. API Contract

Must include:

- endpoint;
- method;
- path;
- headers;
- parameters;
- request body;
- response body;
- error body;
- response codes;
- constraints;
- idempotency;
- security.

### 4. Data Model

Must include:

- logical or physical tables;
- fields;
- constraints;
- indexes;
- relationships;
- CRUD impact;
- retention;
- audit fields.

### 5. Business Rules and Validations

Must include:

- rule ID;
- description;
- condition;
- validation;
- normalization;
- examples;
- failure behavior.

### 6. Error Management

Must include:

- HTTP code if applicable;
- technical error code;
- user message;
- corrective action;
- retry strategy;
- logging;
- audit.

### 7. Edge Cases

Must include:

- data validation edge cases;
- concurrency edge cases;
- security edge cases;
- missing information;
- duplicate processing;
- external failure;
- rollback.

### 8. Dependencies

Must include:

- technical dependencies;
- functional dependencies;
- external services;
- upstream/downstream systems;
- permissions;
- configuration.

### 9. Technical Acceptance Criteria

Must include:

- integration tests;
- functional tests;
- API tests;
- persistence tests;
- security tests;
- performance tests;
- concurrency tests if applicable.

### 10. UML Sequence Diagram

Must provide PlantUML only.

Include nominal flow and at least one alternative or error flow when applicable.