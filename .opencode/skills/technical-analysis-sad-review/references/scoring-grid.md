# Detailed Scoring Grid

## 1. Fidelity to Target SAD — 35 points

Evaluate:

- objective alignment;
- scope alignment;
- exclusions respected;
- functional requirements correctly interpreted;
- architecture decisions respected;
- assumptions preserved;
- no unsupported contradiction;
- no unsupported extension of scope.

High score: the analysis accurately reflects the SAD.
Low score: the analysis contradicts or rewrites the SAD.

## 2. Functional and Business Completeness — 15 points

Evaluate:

- actors;
- business rules;
- use cases;
- scenarios;
- business objects;
- lifecycle/status transitions;
- exceptions;
- document or form handling if applicable.

## 3. Technical and Integration Completeness — 15 points

Evaluate:

- components;
- systems;
- APIs;
- events;
- commands;
- batch processes;
- databases;
- document stores;
- external dependencies;
- orchestration;
- interface contracts.

## 4. Data, Flows, and Persistence — 10 points

Evaluate:

- inbound flows;
- outbound flows;
- sync/async classification;
- data read;
- data created;
- data updated;
- data deleted;
- persistence;
- ownership;
- source of truth;
- retention;
- migration;
- volumetry;
- mapping;
- error handling.

## 5. Architectural Alignment — 10 points

Evaluate:

- architectural key items;
- standards;
- deviations and new technologies;
- alternatives;
- component responsibilities;
- bounded contexts;
- source-of-truth principle;
- coupling control;
- justified architecture choices.

## 6. Security, Compliance, and Audit — 7 points

Evaluate:

- authentication;
- authorization;
- IAM/FedIAM;
- roles;
- mandates;
- service-to-service security;
- sensitive data handling;
- audit;
- logging;
- traceability;
- GDPR;
- licensing impact.

## 7. NFR, Operability, and Testability — 5 points

Evaluate:

- performance;
- availability;
- scalability;
- resilience;
- monitoring;
- alerting;
- logging;
- deployment;
- rollback;
- supportability;
- testability.

## 8. Risks, Assumptions, and Open Points — 3 points

Evaluate:

- explicit risks;
- implicit risks;
- critical dependencies;
- assumptions;
- unresolved decisions;
- questions to analyst;
- questions to SAD owner or architect.