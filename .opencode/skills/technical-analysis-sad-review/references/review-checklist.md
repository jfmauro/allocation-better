# SAD Technical Analysis Review Checklist

## General Understanding

- Is the SAD objective clear?
- Is the scope explicit?
- Are exclusions mentioned?
- Are assumptions listed?
- Are open points visible?
- Are systems and actors identified?

## Functional Coverage

- Are business rules covered?
- Are nominal scenarios covered?
- Are alternative scenarios covered?
- Are exceptions covered?
- Are actors identified?
- Are business objects identified?
- Are statuses or transitions covered when applicable?

## Technical Coverage

- Are components identified?
- Are impacted systems identified?
- Are APIs documented?
- Are events documented?
- Are commands documented?
- Are batch processes documented?
- Are dependencies explicit?
- Are impacts on existing systems described?

## Data

- Are business objects identified?
- Are authoritative sources identified?
- Is data ownership clear?
- Is read data described?
- Is created data described?
- Is updated data described?
- Is deleted data described?
- Is persistence described?
- Is retention described?
- Is volumetry described?
- Is migration described?
- Is classification described?

## Flows

- Are inbound flows described?
- Are outbound flows described?
- Are sequences understandable?
- Is sync/async behavior clear?
- Are errors described?
- Are retries described when applicable?
- Is idempotence described when applicable?
- Is DLQ described when applicable?

## Architecture

- Are decisions explicit?
- Are decisions justified?
- Are alternatives mentioned when needed?
- Are component responsibilities respected?
- Are expected architecture patterns followed when applicable?
- Are deviations and new technologies justified?

## Security

- Are access rights described?
- Are roles described?
- Are IAM/FedIAM integrations described if applicable?
- Are mandates covered when applicable?
- Are sensitive data identified?
- Are logs planned?
- Is auditability covered?
- Is traceability sufficient?

## Non-Functional Requirements

- Are volumes mentioned?
- Is performance addressed?
- Is availability addressed?
- Is scalability addressed?
- Is resilience addressed?
- Is monitoring planned?
- Is exploitability covered?
- Is testability covered?

## Risks

- Are risks listed?
- Are impacts evaluated?
- Are critical dependencies mentioned?
- Are open points clear?
- Are decisions to confirm identified?