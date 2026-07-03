# Architecture Patterns

Use this reference to assess whether a technical analysis is aligned with common SAD architecture expectations.

## Application Patterns

- Microservice-based architecture when decomposition is required.
- Clear frontend/backend separation when UI is present.
- Contract-first APIs when services are exposed or consumed.
- Explicit component responsibilities.
- Separation by bounded context, internal data owner, or clear functional responsibility.

## Integration Patterns

- REST for synchronous consultation or simple command use cases.
- Messaging for asynchronous events, commands, and replies.
- Batch processing for massive, scheduled, or back-office operations.
- Datawarehouse or datamart integration for reporting, analytics, or derived computation.
- Document management system integration for persistent document storage.
- Printing or document generation adapters when official outputs are produced.
- IAM/FedIAM or equivalent identity platform integration for security.
- Service-to-service authentication and authorization for internal APIs.

## Data Patterns

- Dedicated storage per application or microservice when persistence is required.
- Explicit source of truth.
- No direct database coupling across domains unless explicitly justified.
- Clear CRUD responsibilities.
- Retention and volumetry documented for sensitive, regulated, or large-scale data.
- Data classification and auditability documented.
- Lineage and layered storage documented for data-platform scenarios.

## Resilience and Operations Patterns

- Containerized deployment where applicable.
- GitOps or deployment automation where applicable.
- Secrets management.
- Configuration management.
- Health checks.
- Performance endpoints when relevant.
- Centralized logging.
- Audit logging.
- Monitoring and alerting.
- Idempotence, retry, dead-letter handling, and outbox pattern for event-driven flows when applicable.

## Divergence Handling

Do not convert contextual variations into absolute rules.

If the target SAD does not decide, flag the item as an attention point.

If the technical analysis decides without SAD support, flag it as an unsupported assumption.

If the technical analysis contradicts the SAD, prioritize the SAD.