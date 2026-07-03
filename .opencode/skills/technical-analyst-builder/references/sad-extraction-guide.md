# SAD Extraction Guide

When reading a SAD, extract only information useful for producing the technical and functional analysis.

## Extract

- architecture decisions;
- components;
- systems;
- UI/frontend constraints;
- backend services;
- APIs;
- events;
- commands;
- batch processes;
- database technologies;
- data ownership;
- persistence rules;
- source of truth;
- document storage;
- external services;
- infrastructure;
- deployment;
- monitoring;
- logging;
- audit;
- authentication;
- authorization;
- roles;
- permissions;
- data classification;
- privacy constraints;
- NFR;
- performance targets;
- scalability targets;
- resilience expectations;
- availability expectations;
- open points.

## Do Not Infer

Do not infer technologies, systems, or constraints that the SAD does not state.

If the SAD is silent, write:

`Not specified in the SAD — requires architect confirmation.`