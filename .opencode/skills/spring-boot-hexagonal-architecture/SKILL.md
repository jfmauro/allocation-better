---
name: spring-boot-hexagonal-architecture
description: Use this skill to build, extend, or review Spring Boot applications following the hexagonal architecture.
recommendations: Maven modules, domain/application/adapter/bootstrap boundaries, validation, mapping, outbound ports, transactions, and messaging.
compatibility: "claude-code, opencode"
---

# Spring Boot Hexagonal Architecture Skill

## Purpose

Use this skill for professional Spring Boot application creation or review when the application must follow the internal Confluence recommendations on hexagonal architecture.

The goal is to keep business logic independent from technical frameworks by enforcing clear module, package, object, validation, transaction, and messaging boundaries.

## Source scope


If a requested design decision is not covered by these rules, ask for clarification or mark it explicitly as outside the documented Confluence guidance.

## Mandatory project guardrails

Apply these guardrails when generating code in this project context:

- Java 21.
- Spring Boot 4.x.x for the executable application.
- Maven multi-module project.
- H2 only for local/demo database needs.
- Lombok and MapStruct are allowed and expected where useful.
- Constructor injection only; never use field injection with `@Autowired`.
- Controllers are thin HTTP orchestrators only.
- Business logic belongs in service, application, or domain classes; never in controllers.
- Use SLF4J.
- Every public method logs once at method start and once at method end.
- Every log message starts with `+++` and ends with `+++`.
- Unit-test service classes and validate business behavior.
- Avoid unnecessary `@SpringBootTest`.
- Before adding or changing dependencies, verify internal licensing compliance; treat GPL, AGPL, and SSPL as prohibited by default unless explicitly approved.

## Required module structure

Create or preserve this Maven module structure:

- root parent `pom.xml`
- `domain`
- `application`
- `adapter`
- `adapter-in`
- `adapter-out`
- `bootstrap`

Dependency flow must be unidirectional:

- `domain` has no dependency on `application`, adapters, bootstrap, Spring, JPA, web, or messaging technology.
- `application` depends on `domain` only.
- `adapter-in` depends on `application` to call inbound use cases.
- `adapter-out` depends on `domain` to implement outbound ports.
- `bootstrap` depends on adapters and assembles the runtime application.

The root parent `pom.xml` is an aggregator only: declare modules, manage versions, hold common properties, and contain no source code.

## Domain module rules

The `domain` module is the business core.

Use packages under `<root>.domain`:

- `.model` for rich business objects, entities in the DDD sense, and value objects.
- `.port.in` for inbound ports describing what the application can do.
- `.port.out` for outbound ports describing what the application needs.
- `.service` for pure domain services when logic does not belong to one model object.
- `.common` for shared domain-only constructs such as `Notification` or structured errors.

Rules:

- No Spring dependency.
- No JPA annotations such as `@Entity`.
- No Jackson or API serialization annotations such as `@JsonProperty`.
- No generated DTOs.
- No I/O.
- Domain objects are rich and protect their own invariants.
- Use guard clauses in constructors, factory methods, and state-changing methods.
- Prevent invalid domain objects from existing.
- Use UUID (`String` or `java.util.UUID`) as stable business identifier when needed.
- Do not use database technical IDs such as `Long id` as domain identity.
- Allowed helper dependencies must be pure, framework-agnostic, in-memory, and non-I/O, for example Lombok, Reactor types when needed, or generic Apache Commons utilities.

## Application module rules

The `application` module orchestrates use cases.

Use packages under `<root>.application.service`; use focused subpackages such as `.validation` only when they improve clarity.

Rules:

- Implement inbound ports defined in `domain.port.in`.
- Depend on `domain` only.
- Treat this module as protected business core.
- Avoid Spring dependencies; if dependency injection requires Spring, limit it to the absolute minimum such as `spring-context`.
- Do not use `@Transactional` here.
- Do not handle HTTP, serialization, deserialization, database queries, message sending, or generated DTOs.
- Coordinate domain objects and outbound ports.
- Use outbound ports, not infrastructure classes.

## Adapter-in rules

The `adapter-in` module connects external clients to the application.

Use packages under `<root>.adapter.in`:

- `.web.v1` for REST controllers.
- `.web.exception` for `@RestControllerAdvice`.
- `.web.mapper` for MapStruct DTO-to-domain or DTO-to-command mappers.
- `.messaging` for queue listeners.

Rules:

- Controllers call application inbound ports or services.
- Controllers do not contain business logic, mapping logic, or business validation.
- REST DTOs and generated contract objects stay in adapter-in.
- Technical contract validation happens at the boundary before entering the core.
- Message listeners map valid contract DTOs to pure command objects before calling the application.

## Adapter-out rules

The `adapter-out` module connects the application to external systems.

Use packages under `<root>.adapter.out`:

- `.persistence` for JPA or R2DBC repository implementations.
- `.persistence.mapper` for MapStruct domain/entity mapping.
- `.rest` for REST clients and gateways.
- `.soap` for SOAP clients and gateways.
- `.messaging` for message producers and gateways.
- a cohesive transaction-oriented package for transactional workers if useful.

Rules:

- Implement outbound ports from `domain.port.out`.
- Keep technology-specific code here.
- Persistence entities stay here.
- External API clients stay here.
- Queue producers stay here.
- `@Transactional` belongs here when a transactional worker is needed.

## Bootstrap module rules

The `bootstrap` module is the executable application and composition root.

Responsibilities:

- contain the `@SpringBootApplication` class;
- contain global `@Configuration` classes;
- instantiate pure domain services through `@Bean` methods when they are not Spring components;
- wire services, adapters, ports, validators, and mappers;
- include `spring-boot-maven-plugin` for the executable JAR;
- be the only module with a complete view of the application.

## Object and mapping rules

Use three separate object types:

1. Domain objects in `domain`: pure business concepts with behavior and invariants. No JPA, Jackson, OpenAPI, or database technical identity.
2. Persistence entities in `adapter-out`: database schema representation, persistence annotations, and technical primary keys when required.
3. DTOs or generated contract objects in `adapter-in`: API or messaging contracts, serialization annotations, and Jakarta Bean Validation annotations.

MapStruct rules:

- DTO/domain mappers belong in `adapter-in`.
- Entity/domain mappers belong in `adapter-out`.
- No MapStruct mapper belongs in `domain`.
- Ignore generated technical IDs such as `id` when mapping from domain object to persistence entity.

## Outbound port naming

Name the business role in the port and the technology in the adapter implementation.

- Persistence lifecycle: port `[AggregateName]Repository`; adapter `[Technology][AggregateName]Repository`; example `CloseAccountRequestRepository` and `JpaCloseAccountRequestRepository`.
- Synchronous external query: port `[SystemName]ApiGateway` or `[SystemName]ApiClient`; adapter `[Technology][SystemName]ApiGateway` or `[SystemName]RestAdapter`; example `DossierApiGateway` and `WebClientDossierApiGateway`.
- Asynchronous command dispatch: port `[SystemName]CommandGateway`; adapter `[Technology][SystemName]CommandGateway`; example `ProvisionCommandGateway` and `JmsProvisionCommandGateway`.

Choose one coherent convention and apply it consistently.

## REST validation workflow

Use layered validation from technical contract to business rules.

1. API contract validation in `adapter-in`: DTO annotations such as `@NotNull`, `@Size`, `@Pattern`, and controller parameter `@Valid` reject invalid requests before application logic.
2. Central REST error translation in `adapter-in.web.exception`: use `@RestControllerAdvice` to translate validation failures to `400 Bad Request`; use `application/problem+json` when required by the API contract.
3. Domain self-validation: domain constructors, factories, and state changes enforce invariants with guard clauses.
4. Notification pattern: keep a domain `Notification` to collect multiple validation errors; evolve from strings to structured errors (`field`, `code`, `message`) when clients need precise feedback.
5. Domain validators: pure stateless classes, no I/O, return `Notification` for local business rules.
6. Application validators: application-layer classes that use outbound ports for slower external checks and return `Notification`.
7. Application service flow: run domain validation first, run application validation only if no domain errors exist, merge notifications, throw one validation exception if errors exist, then execute the happy path.

## Event queue validation workflow

Use this for asynchronous events where failures must trigger broker redelivery and eventually DLQ.

- Producers must validate event messages against their contract before sending. If validation fails before sending, treat it as a bug and throw an exception with details.
- Listeners in `adapter-in.messaging` deserialize into the contract DTO, manually invoke Jakarta `Validator`, and reject invalid messages before the core is called.
- On contract validation failure, log details and throw an exception so the broker handles redelivery or DLQ.
- On success, map the contract DTO to a pure command and call the application port.
- If the application port returns `Mono` or `Flux`, block inside the synchronous `@JmsListener`; the method must complete before acknowledgement.
- If business validation fails after the contract is valid, throw an exception. Do not ignore the message or complete gracefully.

## Command/reply queue validation workflow

Use this for command queues that require a structured reply to `ReplyTo`.

- Producers must validate commands before sending.
- Replies must also be validated before sending.
- Define an outbound reply port in `domain.port.out`, for example a gateway with success and failure methods.
- The listener performs technical contract validation first.
- If contract validation fails, use the reply gateway to send a failure response and stop; this prevents technical rejection code and generated DTOs from leaking into the core.
- If validation succeeds, map the DTO to a pure command and call the application port.
- The application service performs business validation and uses the same reply gateway for business failure or success response.

## Transactional worker pattern

Use this when several operations must succeed or fail together.

Rules:

- Do not put `@Transactional` in `domain` or `application`.
- Define a domain outbound port that describes the business capability; do not name the port after transactions.
- Implement the port in `adapter-out` with a worker class carrying `@Transactional`.
- The worker may orchestrate persistence repositories, message gateways, and domain state changes inside the transaction.
- The application service calls the port without knowing a transaction is being managed.

## Implementation method for the AI agent

When this skill is active:

1. Inspect the repository structure before proposing changes.
2. Preserve existing naming and package conventions when they do not contradict this skill.
3. For a greenfield project, propose the Maven module tree first.
4. Place each class in the module and package that matches its responsibility.
5. Do not introduce a new framework, pattern, or dependency unless required by the documented architecture or explicitly requested.
6. Generate small cohesive classes and focused methods.
7. Add or update tests for service/application behavior.
8. Run the most focused available tests when possible.
9. In the final response, report which boundaries, validation rules, mapping rules, transaction rules, and tests were applied.

## Review checklist

Before considering work complete, verify:

- Domain has no Spring, JPA, Jackson, generated DTO, or I/O dependency.
- Application depends only on domain and has no `@Transactional`.
- Adapter-in owns REST controllers, exception handlers, API DTOs, contract validation, message listeners, and inbound mappers.
- Adapter-out owns persistence entities, repositories, external clients, message producers, outbound mappers, and transactional workers.
- Bootstrap is the composition root and only executable module.
- DTOs, entities, and domain objects are not mixed.
- MapStruct mappers are only in adapters.
- Outbound ports use consistent role-based names.
- REST validation returns clean client errors.
- Event listener failures trigger broker redelivery or DLQ.
- Command/reply validation uses the reply gateway for both technical and business failures.
- Constructor injection is used everywhere.
- Public methods have mandatory SLF4J start/end logs.
- Service behavior is covered by unit tests.