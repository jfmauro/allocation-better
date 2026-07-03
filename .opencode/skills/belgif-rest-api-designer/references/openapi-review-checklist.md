# OpenAPI review checklist for Belgif REST APIs

## Contract metadata
- [ ] OpenAPI version is declared and supported by target tooling.
- [ ] `info.title`, `info.version`, contact/license if applicable.
- [ ] Servers use appropriate absolute URLs for environments.
- [ ] Tags group operations by business capability.

## Paths and methods
- [ ] Paths are resource-oriented nouns, not RPC verbs.
- [ ] Collections and items are modeled distinctly.
- [ ] HTTP methods match semantics and idempotency.
- [ ] No sensitive data appears in paths or query parameters.

## Parameters and bodies
- [ ] Parameters have schema, description, examples and required flag.
- [ ] Request bodies specify `required` explicitly.
- [ ] Media type is documented, typically `application/json`.
- [ ] Sensitive searches use body-based criteria when necessary.

## Responses and errors
- [ ] Success responses cover expected synchronous/asynchronous outcomes.
- [ ] Creation returns `201` and `Location` where appropriate.
- [ ] Error responses use standardized Problem schema.
- [ ] Problems avoid leaking sensitive values.
- [ ] Common responses are reusable components.

## Schemas
- [ ] Required arrays are explicit.
- [ ] Identifiers are strings unless numeric semantics are guaranteed.
- [ ] Dates/times use standard formats and time zone rules.
- [ ] Enums are documented.
- [ ] `oneOf`/`allOf` discriminator mappings are complete and valid.
- [ ] Reusable Belgif schemas are referenced where applicable.

## Collections
- [ ] Pagination parameters and metadata are documented.
- [ ] Default/max page size documented.
- [ ] Sorting and filtering semantics documented.

## Security
- [ ] Security schemes are defined.
- [ ] Operation-level security requirements match the schemes.
- [ ] Scopes are documented.
- [ ] Unsecured operations have explicit rationale.

## Publication readiness
- [ ] Examples validate against schemas.
- [ ] Breaking changes are versioned.
- [ ] Deprecations are explicit.
- [ ] Official Belgif validator has been run or scheduled.
