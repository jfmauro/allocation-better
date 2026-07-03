# Conflict Resolution Rules

## Business Analysis versus SAD

If a business rule conflicts with a SAD constraint:

1. Describe the business requirement.
2. Describe the SAD constraint.
3. Mark the conflict as unresolved.
4. Propose a clarification question.
5. Do not make an irreversible design decision.

## Missing SAD Detail

If the business analysis requires implementation detail not present in the SAD:

- generate a logical specification;
- avoid naming unconfirmed technologies;
- mark the technical choice as requiring architect confirmation.

## Missing Business Detail

If the SAD describes a component but the business analysis does not require it:

- do not force it into the user story;
- mention it only if it is a required architectural dependency.

## Complementary Context

Use complementary context only if explicitly provided and relevant.

If complementary context overrides the SAD or business analysis, mark the override explicitly.
``