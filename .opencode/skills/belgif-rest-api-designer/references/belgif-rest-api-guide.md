# Belgif REST API Guide — operational synthesis for agents

Source of truth: https://www.belgif.be/specification/rest/api-guide/ . This file is a practical synthesis for API design assistance; the official Belgif guide and validator remain authoritative.

## 1. Purpose and conformance
The Belgif REST guide is a living style guide created collaboratively by Belgian government institutions to improve compatibility and interoperability of RESTful services. Its normative words such as MUST, SHOULD and MAY follow the RFC 2119/RFC 8174 convention when written in capitals.

Belgif also maintains related assets: reusable OpenAPI common definitions, a REST guide validator for OpenAPI compliance, a Java library for standardized Problem messages, FedVoc semantic definitions, and security guidance based around OAuth 2.0 / OpenID Connect.

## 2. Core API design principles
- Design around resources, not implementation procedures.
- Prefer stable, understandable collection and item URIs.
- Treat the OpenAPI contract as the precise interface between provider and consumer.
- Make behavior explicit: status codes, security, errors, pagination, examples, formats, and constraints.
- Favor reusable Belgif schemas for common domains such as problem, time, person, identifiers, location, organization, money, healthcare and cloud events where applicable.

## 3. URI and resource design
Recommended patterns:

```text
GET    /v1/declarations
POST   /v1/declarations
GET    /v1/declarations/{declarationId}
PUT    /v1/declarations/{declarationId}
PATCH  /v1/declarations/{declarationId}
DELETE /v1/declarations/{declarationId}
GET    /v1/declarations/{declarationId}/attachments
POST   /v1/declarations/{declarationId}/attachments
```

Guardrails:
- Use nouns for resources; avoid RPC-style verbs in paths.
- Use plural collection names.
- Keep identifiers opaque unless a public domain identifier is explicitly part of the contract.
- Do not expose sensitive data in path segments or query parameters.
- Use sub-resources for containment or lifecycle dependence; otherwise use top-level resources and hyperlinks/IDs.

## 4. HTTP method semantics
- `GET`: safe retrieval; no state mutation.
- `POST`: create a subordinate resource or submit a processing request when the server determines the result URI or action semantics.
- `PUT`: full replacement at a known URI; idempotent.
- `PATCH`: partial update; document patch format and concurrency expectations.
- `DELETE`: remove or cancel; document whether deletion is logical or physical.

## 5. Status code guidance
Typical mapping:
- `200 OK`: synchronous read/update result.
- `201 Created`: creation; include `Location` when a new resource URI exists.
- `202 Accepted`: asynchronous processing accepted; return a status resource or tracking URI.
- `204 No Content`: successful operation without response body.
- `400 Bad Request`: syntactic or generic client error.
- `401 Unauthorized`: missing/invalid authentication.
- `403 Forbidden`: authenticated but not authorized.
- `404 Not Found`: resource not found or intentionally undisclosed.
- `409 Conflict`: state conflict, duplicate, business conflict.
- `412 Precondition Failed`: failed conditional request.
- `422 Unprocessable Entity`: syntactically valid but semantically invalid input, when used by the API style.
- `429 Too Many Requests`: rate limit.
- `500/502/503/504`: server or gateway errors.

Every documented error response must have a Problem body unless a specific exception is justified.

## 6. Error model: Problem responses
Use a standardized problem representation inspired by RFC 7807/9457 and Belgif common problem schemas. Include stable machine-readable type/code information and human-readable title/detail. Good fields:

```json
{
  "type": "urn:problem-type:belgif:badRequest",
  "title": "Bad Request",
  "status": 400,
  "detail": "The request contains invalid parameters.",
  "instance": "urn:uuid:00000000-0000-0000-0000-000000000000"
}
```

For validation errors, include invalid parameter details when supported by the reused Belgif problem schema. Avoid leaking sensitive values in Problem details.

## 7. Data, privacy and sensitivity
- Never put sensitive data in URLs or headers because these are commonly logged, cached, bookmarked, and propagated.
- Prefer POST search resources with a request body for sensitive multi-criteria searches.
- Redact examples and logs.
- Avoid echoing secrets, national identifiers or personal data in errors.

## 8. JSON and schema design
- Use clear property names; prefer lower camel case.
- Mark required properties explicitly.
- Use `format` where meaningful: `date`, `date-time`, `uuid`, etc.
- Prefer strings for identifiers that may contain leading zeros or non-numeric semantics.
- Model monetary values and dates with Belgif reusable schemas when possible.
- Document enumerations with business meaning.
- Avoid `additionalProperties: true` unless extensibility is intentional.
- If using `oneOf`/`allOf` polymorphism, define discriminator and mapping completely; mapping values must be valid URI references where required by OpenAPI/Belgif validation.

## 9. Pagination, filtering and sorting
For collection resources:
- Define pagination parameters and response metadata consistently.
- Declare default and maximum page size.
- Document sort fields and sort direction.
- Document filter semantics, exact matching vs partial matching, case sensitivity, date ranges, and time zone behavior.
- For sensitive search criteria, use a search endpoint with body instead of query parameters.

Example response shape:

```json
{
  "items": [],
  "page": { "limit": 20, "offset": 0, "total": 0 }
}
```

Adapt to the Belgif reusable pagination schema if used in your organization.

## 10. Headers and cross-cutting concerns
Document standard and custom headers explicitly. Consider:
- `Authorization` via OAuth2/OIDC.
- Correlation/request ID headers where used by the institution.
- `Location` on creation/asynchronous operations.
- `ETag`, `If-Match`, `If-None-Match` for optimistic concurrency and caching.
- `Retry-After` for throttling or temporary unavailability.
Do not invent institution-specific headers when a Belgif/common convention exists.

## 11. Security
- Define OpenAPI `securitySchemes` and operation-level `security` consistently.
- If OAuth2/OIDC is used, document flows, token URL, scopes, and operation scope requirements.
- Do not leave operations unsecured by accident.
- Avoid describing security secret values in examples.

## 12. OpenAPI contract quality
A Belgif-grade OpenAPI must:
- Be OpenAPI 3.x.
- Use clear `info`, `servers`, `tags`, `operationId`, summaries and descriptions.
- Define request bodies with explicit required flags.
- Define schemas in components and reuse them.
- Include examples for success and failure.
- Include all common failure responses.
- Pass the official Belgif validator before publication.

## 13. Design review rubric
Classify findings as:
- MUST FIX: breaks Belgif, interoperability, security, or contract correctness.
- SHOULD FIX: not fatal but weakens predictability or usability.
- ACCEPTABLE: compliant or reasonable given context.
- ASSUMPTION: requires confirmation from team-specific standards or the official guide.
