# Project Rules

## Responses

- Keep responses concise and to the point unless the user asks otherwise.

## Knowledge inputs

- All functional and contextual requirements live under `knowledge/`.
- The framework is input-agnostic: any file dropped in `knowledge/` (Markdown, text, exported Confluence page, transcript, etc.) is treated as authoritative input.
- Agents and commands MUST reference `knowledge/**` rather than hardcoded filenames or page IDs.
- `knowledge/README.md` lists the inputs and any precedence rules when they overlap.

## Planning mode

- Always ask clarifying questions before producing a plan.
- Never assume design, tech stack, or features beyond what is stated in `knowledge/` and `DESIGN.md`.
- Use subagents (explore, scout) to research before planning.
- Review the plan with the `sad-architect-reviewer` agent before presenting to the user.

## Build mode

- Never implement features yourself when possible — delegate to specialist subagents.
- Identify parallelizable tasks and dispatch subagents concurrently, in a single message, capped at 4 concurrent specialists.
- Act as a coordinator: delegate, review, integrate.
- Rely on LSP diagnostics for compile-level feedback while editing; do not run `mvn compile` per micro-cycle.
- After completing a full hexagonal layer, run once: mvn -q test -pl <module>

## Tech stack

- Java 21, Spring Boot 4.x (LTS).
- Maven multi-module project.
- H2 for local/demo database.
- Lombok and MapStruct are expected where useful.
- Vanilla HTML, CSS, and JavaScript for the frontend (optional — only when the project includes a frontend module; skip if the project is pure backend).
- Spring AI with Groq scaffolded for future AI features.

## Technical architecture

- Hexagonal architecture (Ports and Adapters) is mandatory.
- Load the `spring-boot-hexagonal-architecture` skill for detailed rules.
- When the `spring-boot-hexagonal-architecture` skill is loaded, its rules take precedence over generic `java-springboot` advice for transaction placement, module boundaries, and dependency flow.
- Module structure: domain, application, adapter-in, adapter-out, bootstrap.
- Dependency flow is strictly unidirectional: domain has zero framework imports.
- Transactions are managed at adapter-out worker level (@Transactional), never in domain or application.

## Code standards

- Constructor injection only; never field injection with @Autowired.
- Controllers are thin HTTP orchestrators only; zero business logic.
- Business logic belongs in domain objects and application services.
- SLF4J for logging. Log at architectural boundaries only: application service public methods and adapter public methods (adapter-in controllers, adapter-out workers). Do NOT log inside domain entities, value objects, or factory methods.
- Each logged method logs once at start and once at end; every log message starts with `+++` and ends with `+++`.
- All code, variable names, comments, and text in English.

## Locking

- Use @Version (optimistic locking) and @Lock(PESSIMISTIC_WRITE) (pessimistic locking).
- Choose per use case: pessimistic for high-contention writes, optimistic for low-contention updates.

## Testing

- Always use TDD at class granularity: for each class, write the full JUnit5 test class first (RED) covering all its behaviors, then implement the class to green (GREEN), then refactor.
- The vertical slice is the class, not the method: never batch tests across multiple classes, and never write all classes' tests before any implementation.
- JUnit5 and Mockito for unit tests.
- @WebMvcTest for controller tests, @DataJpaTest for repository tests.
- Avoid @SpringBootTest unless testing full integration.
- Run the module test suite once per layer, not per behavior.
- Load the `tdd` skill for detailed methodology.

## Database schema changes

- Design production-ready schemas with proper indexes and constraints.
- Load the `database-schema-designer` skill for guidelines.

## UI design

- Follow the design system defined in `DESIGN.md` (loaded automatically via instructions).
- Load the `frontend-design` skill for aesthetic guidelines.
- Apply only when the project includes a frontend module.

## Quality gates

- Every hexagonal layer must pass two reviews before completion:
  1. `spec-reviewer`: conformity to the requirements in `knowledge/`.
  2. `code-reviewer`: security, performance, architecture, test adequacy.
- Reviews run once per layer over the full layer changeset. Each reviewer allows at most one corrective batch; if still CHANGES_REQUESTED, escalate to the user.
- No layer is complete until both reviewers return APPROVED.

## Modes, refactoring, extension, and No-SAD

The framework supports four modes built on top of the same five-command pipeline, with the SAD review gate that can be enabled or disabled per execution.

### Modes

- `greenfield` (default): build a system from `knowledge/`. No existing code expected.
- `extension-business`: an existing project receives a new business analysis under `knowledge/inbox/`; the framework adds functionality while preserving existing contracts.
- `refactor-business`: a change request modifies existing behavior; the framework rewrites the impacted code while preserving non-regression contracts.
- `refactor-technical`: technical-debt remediation only; no business change is allowed.

### SAD check

- `with-sad` (default): the SAD review gate is mandatory; the SAD lives under `knowledge/`.
- `no-sad`: the SAD review gate is skipped; reviewers operate in best-effort mode against generic architecture standards. A warning is inscribed in every artifact produced under this mode.

### Command parameters

| Command | `$1` | `$2` | `$3` |
|---|---|---|---|
| `/analyse` | mode | sad-check | archive |
| `/plan` | mode | sad-check | — |
| `/build` | layer | mode (greenfield / extension / refactor) | review-cadence (layer / step) |
| `/document` | mode (greenfield / extension / refactor) | — | — |
| `/release` | mode (greenfield / extension / refactor) | sad-check | — |

Defaults reproduce the legacy greenfield behavior, with the review cadence defaulting to per-layer.
