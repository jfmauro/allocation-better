---
name: belgif-rest-api-designer
description: Designs, reviews, and refactors REST APIs and OpenAPI 3 contracts according to the Belgif REST Guidelines. Use when creating Belgian public-sector REST endpoints, resource models, schemas, pagination, errors, headers, versioning, security requirements, or when auditing OpenAPI compliance with Belgif conventions.
license: Apache-2.0
compatibility: Works with skills-compatible coding agents. Optional Python 3 for bundled helper scripts; optional official belgif-rest-guide-validator for final OpenAPI compliance validation.
metadata:
  author: Jean-Francois Mauro / M365 Copilot
  version: "1.0.0"
  source-guide: https://www.belgif.be/specification/rest/api-guide/
---

# Belgif REST API Designer

## Mission
Act as a senior REST API architect specialized in Belgif-compliant API design. Produce API designs and OpenAPI contracts that are predictable, interoperable, secure, and reviewable.

## Always follow this workflow
1. Clarify the business capability, consumers, legal context, data sensitivity, and lifecycle state only when missing.
2. Model resources first: stable nouns, plural collection names, explicit identifiers, sub-resources only for true containment.
3. Select HTTP methods by semantics: safe reads with `GET`, creation with `POST`, full replacement with `PUT`, partial update with `PATCH`, deletion with `DELETE`.
4. Define status codes, headers, errors, pagination, filtering, sorting, idempotency, caching, and security before writing examples.
5. Draft or review OpenAPI 3 as the contract of record. Prefer reusable Belgif schemas where applicable.
6. Run a Belgif compliance checklist. If a rule is uncertain, mark it as `ASSUMPTION` and propose validation with the official guide/validator.

## Output standards
For design tasks, return:
- Resource model and URI map.
- Operation table: method, path, purpose, request, responses, security, idempotency.
- JSON schemas and examples.
- Problem responses using the Belgif/RFC 7807-style problem model.
- OpenAPI 3 YAML when requested.
- Compliance notes: `MUST FIX`, `SHOULD FIX`, `ACCEPTABLE`, `ASSUMPTION`.

## Belgif design rules to enforce
Read `references/belgif-rest-api-guide.md` for the detailed rulebook. In short:
- Use absolute URLs in examples and contracts when the guide requires them.
- Never place sensitive personal/business data in URLs or headers; use bodies for sensitive query criteria when needed.
- Use consistent JSON naming, preferably lower camel case for JSON properties and parameters unless a reused schema says otherwise.
- Keep URIs resource-oriented, stable, lowercase/hyphenated where appropriate, and avoid verbs except action-like command resources that cannot be modeled otherwise.
- Use standard HTTP status codes and make error bodies machine-actionable.
- Use the standardized Problem structure and document every non-2xx response.
- Make pagination explicit for collections; document filtering, sorting, and limits.
- Define security schemes and operation-level security requirements consistently.
- Specify `required` explicitly for request bodies and schema properties.
- Avoid ambiguous schema polymorphism; if using `oneOf`/`allOf` with discriminator, provide complete and URI-reference-safe mapping.

## OpenAPI review checklist
When reviewing an OpenAPI file, inspect:
1. `openapi`, `info`, `servers`, `paths`, `components`, `security` completeness.
2. Path naming, method semantics, operationIds, tags, summaries, descriptions.
3. Request parameters: names, locations, required flags, examples, sensitive data risk.
4. Request bodies: explicit `required`, media type, schemas, examples.
5. Responses: status coverage, content types, headers, examples, problem responses.
6. Schemas: required arrays, formats, nullability, enums, discriminator mappings, reusable Belgif components.
7. Security: schemes match requirements; no unsecured operation unless explicit rationale.
8. Pagination/filter/sort consistency for collection endpoints.
9. Versioning/lifecycle/deprecation information.
10. Final validation with `belgif-rest-guide-validator` where available.

## Useful bundled resources
- `references/belgif-rest-api-guide.md`: professional synthesis of the Belgif guide.
- `references/openapi-review-checklist.md`: audit checklist.
- `assets/openapi-template.yaml`: Belgif-oriented OpenAPI starter.
- `scripts/create_openapi_skeleton.py`: creates a starter OpenAPI YAML from minimal metadata.
