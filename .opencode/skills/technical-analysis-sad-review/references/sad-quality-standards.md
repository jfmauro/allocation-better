# SAD Quality Standards

This reference defines the expected quality level for validating a technical analysis against a target SAD.

## Expected SAD Structure

A strong SAD usually contains:

1. General Information
2. Version History
3. Context
    - solution summary;
    - current situation;
    - target situation;
    - problem statement when relevant.
4. Acronyms and Glossary
5. Referenced Documents
6. Requirements
    - assumptions;
    - functional requirements;
    - non-functional requirements;
    - prioritization when applicable;
    - data classification when applicable.
7. Solution
    - architectural key items;
    - standards;
    - deviations and new technologies;
    - alternatives.
8. Functional Architecture
    - logical view;
    - context view;
    - process view;
    - use cases;
    - sequence, C4, or BPMN diagrams.
9. Application and Integration Architecture
    - information systems;
    - logical and container views;
    - exposed services;
    - consumed services.
10. Infrastructure Architecture
- technical properties;
- resource identifiers;
- deployment model;
- monitoring;
- sizing.
11. Data Architecture
- data model;
- storage and persistence;
- data flow;
- retention;
- migration;
- volumetry.
12. Security Architecture
- user types;
- authentication;
- authorization;
- audit;
- IAM/FedIAM components if applicable.
13. Licensing Impact, when relevant.

## Expected Technical Analysis Quality

A technical analysis is complete when it:

- correctly restates the SAD objective and scope;
- covers all components and impacted systems;
- describes inbound and outbound flows;
- identifies data, storage, ownership, retention, and volumetry;
- describes APIs, events, commands, batch processes, and contracts when applicable;
- restates architecture decisions and their rationale;
- addresses security, IAM/FedIAM, audit, logs, and sensitive data when applicable;
- covers NFR, sizing, monitoring, resilience, and availability;
- identifies assumptions, risks, limitations, and open points;
- distinguishes SAD facts from analyst assumptions.

## Insufficient Analysis Indicators

An analysis is insufficient when:

- it only summarizes business intent;
- technical components are missing;
- integrated systems are not all cited;
- flows are vague or not directional;
- security is generic;
- NFR are ignored;
- data is not described;
- decisions are not justified;
- open points are presented as resolved decisions.

## Ambiguity Indicators

An analysis is ambiguous when:

- responsibilities between components are confused;
- synchronous and asynchronous behavior is unclear;
- source of truth and local storage are mixed up;
- IAM/FedIAM roles are undefined;
- a critical dependency is mentioned without impact analysis;
- the SAD contains unresolved TBD, N/A, or placeholders.