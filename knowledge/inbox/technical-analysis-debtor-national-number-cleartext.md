# Technical Analysis: Debtor National Number in Cleartext

## Context

The debtor intake UI currently exposes technical headers and dual natural-person identifiers.
The requested change simplifies the user experience and replaces the natural-person identifier
model with a single cleartext `nationalNumber` field.

## Scope

- Replace `nationalNumberHash` and `nationalNumberEncrypted` with `nationalNumber`.
- Keep `enterpriseNumber` for enterprise debtors.
- Hide the irrelevant identifier field in the UI depending on debtor type.
- Keep `Idempotency-Key` and `X-Correlation-Id` internal to the client code, not visible in the form.

## Functional Rules

- `ENTERPRISE` debtor:
  - requires `enterpriseNumber`
  - hides `nationalNumber`
- `NATURAL_PERSON` debtor:
  - requires `nationalNumber`
  - hides `enterpriseNumber`
- `nationalNumber` is stored and transmitted in cleartext for now.

## API Contract Impact

- `POST /debtors` request body changes to:
  - `debtorType`
  - `displayName`
  - `nationalNumber`
  - `enterpriseNumber`
- The request headers remain required by the backend:
  - `Idempotency-Key`
  - `X-Correlation-Id`

## Domain Impact

- `CreateDebtorCommand` must accept `nationalNumber` instead of split fields.
- `Debtor` must store a single cleartext `nationalNumber` for natural persons.
- Guard clauses must enforce type-based exclusivity.

## Persistence Impact

- Debtor persistence must replace the two legacy columns with `national_number`.
- Existing unique and search semantics must be adapted to the new field.
- A schema migration is required because automatic update will not safely remove legacy columns.

## Frontend Impact

- The debtor creation form must be simplified.
- Technical headers must not be user-editable.
- The visible identifier field must change dynamically with debtor type.

## Risks

- Cleartext storage of national numbers increases sensitivity.
- Legacy records may require a migration decision if prior encrypted/hash values exist.
- Any test or mapper still using legacy fields will fail until fully aligned.

## Acceptance Criteria

- Natural persons can be created using cleartext `nationalNumber`.
- Enterprises can be created using `enterpriseNumber`.
- The UI no longer exposes technical headers or legacy natural-person fields.
- The backend persists the new model consistently.
- Automated tests pass for frontend, adapter, application, domain, and persistence layers.
