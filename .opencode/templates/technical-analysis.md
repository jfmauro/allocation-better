# Technical Analysis

> **Purpose of this template**  
> This document is a complete technical-analysis template intended to be used by an agentic AI during an application development process.  
> It must transform business/user-story input into an implementation-ready technical analysis.
>
> The expected hierarchy is:
>
> **EPIC → FEATURE → USER STORY → TECHNICAL ANALYSIS**
>
> Each User Story must have its own complete and self-contained technical analysis. A developer must be able to implement the User Story without requiring additional clarification.

---

## Document metadata

| Field | Value |
|-------|-------|
| Epic ID | `<EPIC-ID>` |
| Epic title | `<EPIC-TITLE>` |
| Feature ID | `<FEATURE-ID>` |
| Feature title | `<FEATURE-TITLE>` |
| User Story ID | `<US-ID>` |
| User Story title | `<US-TITLE>` |
| Priority | `<Critical / High / Medium / Low>` |
| Project phase / release | `<PHASE / MVP / RELEASE>` |
| Author | `<AUTHOR>` |
| Reviewer(s) | `<REVIEWERS>` |
| Version | `<VERSION>` |
| Status | `<Draft / In review / Approved>` |
| Last update | `<YYYY-MM-DD>` |

---

## 1. Scope and objectives

### 1.1 User Story reference

- **ID:** `<US-ID>`
- **Title:** `<descriptive title of the User Story>`
- **Priority:** `<Critical / High / Medium / Low>` `(<project phase / MVP / release>)`
- **As a:** `<user type / role / actor>`
- **I want:** `<desired action or capability>`
- **So that:** `<expected business value / benefit>`

### 1.2 Functional scope

Describe precisely what is included in the User Story.

The scope must cover:

- User-visible behavior.
- Screens, forms, pages, components or interfaces involved.
- User interactions and navigation.
- Business rules triggered by the feature.
- Data created, read, updated, deleted or displayed.
- API interactions required by the feature.
- Persistence requirements.
- Error handling visible to the user.

**In scope:**

- `<item 1>`
- `<item 2>`
- `<item n>`

**Out of scope:**

- `<item 1>`
- `<item 2>`
- `<item n>`

### 1.3 Technical objective

Describe what must be implemented technically.

Include, when applicable:

- Front-end components.
- Back-end services.
- API endpoints.
- Database changes.
- Security checks.
- Data validation.
- Integrations with internal or external systems.
- Logging, monitoring or audit requirements.
- Performance considerations.

**Technical objective:**

`<Concise but complete description of the technical implementation objective.>`

### 1.4 Technical prerequisites

List all technical components, services, libraries, configurations or previous User Stories required before implementation can start.

| Prerequisite | Type | Description | Status |
|--------------|------|-------------|--------|
| `<component/service/US>` | `<Technical / Functional / Infrastructure / Security>` | `<description>` | `<Available / To be created / Unknown>` |
| `<component/service/US>` | `<Technical / Functional / Infrastructure / Security>` | `<description>` | `<Available / To be created / Unknown>` |

### 1.5 Expected deliverables

The implementation of this User Story must deliver:

- `<source code component>`
- `<API endpoint>`
- `<database migration>`
- `<unit tests>`
- `<integration tests>`
- `<security tests>`
- `<technical documentation update>`
- `<deployment/configuration change>`

---

## 2. Actors and external systems

### 2.1 Actors

List all human or technical actors involved in the feature.

| Actor | Type | Description | Permissions / Responsibilities |
|-------|------|-------------|--------------------------------|
| `<Actor name>` | `<Human / System / Service>` | `<description>` | `<permissions and responsibilities>` |
| `<Actor name>` | `<Human / System / Service>` | `<description>` | `<permissions and responsibilities>` |

### 2.2 Internal systems and components

List all internal application components involved.

| Component | Layer | Role in this User Story | Input | Output |
|-----------|-------|--------------------------|-------|--------|
| `<UI component>` | `<Frontend>` | `<role>` | `<input>` | `<output>` |
| `<Controller/API resource>` | `<Backend/API>` | `<role>` | `<input>` | `<output>` |
| `<Service>` | `<Backend/Domain>` | `<role>` | `<input>` | `<output>` |
| `<Repository/DAO>` | `<Persistence>` | `<role>` | `<input>` | `<output>` |
| `<Database table>` | `<Database>` | `<role>` | `<input>` | `<output>` |

### 2.3 External systems

List all external systems or services integrated with this User Story.

| External system | Role | Integration type | Data exchanged | Availability / SLA | Failure impact |
|-----------------|------|------------------|----------------|--------------------|----------------|
| `<system>` | `<role>` | `<REST / SOAP / Event / File / Queue / Other>` | `<data>` | `<SLA if known>` | `<impact>` |
| `<system>` | `<role>` | `<REST / SOAP / Event / File / Queue / Other>` | `<data>` | `<SLA if known>` | `<impact>` |

### 2.4 Authentication and authorization context

Describe how actors are authenticated and what authorization checks apply.

- **Authentication mechanism:** `<token / session / OAuth2 / SSO / certificate / other>`
- **Authorization model:** `<RBAC / ABAC / ownership check / technical role / other>`
- **Required role(s):** `<role list>`
- **Required permission(s):** `<permission list>`
- **Authorization failure behavior:** `<HTTP status / UI message / redirect / audit event>`

---

## 3. Use cases

> Each use case must be explicit enough to derive the implementation flow, API calls, validation rules, errors and tests.

### UC-001 - `<name>`

- **Goal:** `<business or technical goal>`
- **Primary actor:** `<actor>`
- **Supporting actors / systems:** `<actors or systems>`
- **Trigger:** `<event that starts the use case>`

#### Preconditions

- `<precondition 1>`
- `<precondition 2>`
- `<precondition n>`

#### Main flow

```text
Step 1: <description>
    ↓
Step 2: <description>
    ↓
Step n: <description>
    ↓
Success: <final state, confirmation, redirection or persisted result>
```

#### Alternates

##### UC-001-A1 - `<alternate name>`

- **Condition:** `<condition triggering the alternate>`
- **Flow:**
    1. `<step>`
    2. `<step>`
    3. `<step>`
- **Result:** `<expected final behavior>`

##### UC-001-A2 - `<alternate name>`

- **Condition:** `<condition triggering the alternate>`
- **Flow:**
    1. `<step>`
    2. `<step>`
- **Result:** `<expected final behavior>`

#### Exceptions

| Exception ID | Scenario | Expected behavior |
|--------------|----------|-------------------|
| `EX-001` | `<exception scenario>` | `<expected behavior>` |
| `EX-002` | `<exception scenario>` | `<expected behavior>` |

#### Postconditions

- `<postcondition 1>`
- `<postcondition 2>`
- `<postcondition n>`

---

### UC-002 - `<name>`

- **Goal:** `<business or technical goal>`
- **Primary actor:** `<actor>`
- **Supporting actors / systems:** `<actors or systems>`
- **Trigger:** `<event that starts the use case>`

#### Preconditions

- `<precondition 1>`
- `<precondition 2>`

#### Main flow

```text
Step 1: <description>
    ↓
Step 2: <description>
    ↓
Success: <final state>
```

#### Alternates

- `<alternate flow 1>`
- `<alternate flow 2>`

#### Postconditions

- `<postcondition 1>`
- `<postcondition 2>`

---

## 4. Functional requirements

| ID | Description | Source |
|----|-------------|--------|
| `FR-001` | The system must allow `<actor>` to `<action>` in order to `<business value>`. | `<US-ID / UC-ID / business rule>` |
| `FR-002` | The system must display `<screen/interface/data>` with `<mandatory elements>`. | `<US-ID / UC-ID>` |
| `FR-003` | The system must validate `<field/business object>` according to `<rule>`. | `<RG-ID / UC-ID>` |
| `FR-004` | The system must persist `<data>` in `<table/storage>` when `<event>` occurs. | `<UC-ID>` |
| `FR-005` | The system must return `<API response>` when `<API endpoint>` is called successfully. | `<API contract>` |
| `FR-006` | The system must handle `<error condition>` by `<expected behavior>`. | `<error management>` |

### 4.1 User interface specifications

#### 4.1.1 Screen / interface: `<screen or interface name>`

**Purpose:**  
`<Describe the role of the screen/interface.>`

**UI components:**

| Component | Type | Description | Properties / Behavior |
|-----------|------|-------------|------------------------|
| `<component>` | `<input/button/table/modal/etc.>` | `<description>` | `<properties and behavior>` |
| `<component>` | `<input/button/table/modal/etc.>` | `<description>` | `<properties and behavior>` |

**Rules:**

- `<validation or behavior rule 1>`
- `<validation or behavior rule 2>`
- `<navigation or action rule>`

**Navigation:**

| Trigger | Destination / Action | Conditions |
|---------|----------------------|------------|
| `<click / submit / cancel>` | `<destination or action>` | `<conditions>` |
| `<click / submit / cancel>` | `<destination or action>` | `<conditions>` |

#### 4.1.2 Screen / interface: `<screen or interface name>`

**Purpose:**  
`<Describe the role of the screen/interface.>`

**UI components:**

| Component | Type | Description | Properties / Behavior |
|-----------|------|-------------|------------------------|
| `<component>` | `<input/button/table/modal/etc.>` | `<description>` | `<properties and behavior>` |

**Rules:**

- `<rule 1>`
- `<rule 2>`

### 4.2 Field validation rules

| Field | Type | Mandatory | Format | Length | Validation |
|-------|------|-----------|--------|--------|------------|
| `<field_1>` | `<type>` | `<Yes/No>` | `<expected format>` | `<min-max>` | `<regex, rule or reference to RG-ID>` |
| `<field_2>` | `<type>` | `<Yes/No>` | `<expected format>` | `<min-max>` | `<regex, rule or reference to RG-ID>` |

**Real-time validation:**

- `<validation mechanism 1>`
- `<validation mechanism 2>`
- `<visual indicator or error message behavior>`

### 4.3 Draft saving / state preservation

Use this subsection only if the User Story requires draft persistence, temporary state or form recovery.

- **Trigger:** `<event triggering draft save>`
- **Storage:** `<local storage / session storage / backend / database / cache>`
- **Storage key:** `<key format>`
- **Expiration:** `<validity duration>`
- **Restoration:** `<recovery mechanism>`
- **Conflict behavior:** `<behavior if existing draft conflicts with server state>`

### 4.4 API contract

> Document every endpoint required by the User Story. Repeat the following subsection for each endpoint.

#### 4.4.1 Endpoint - `<endpoint description>`

```http
<METHOD> <path/to/resource>
```

**Purpose:**  
`<Explain what this endpoint does and when it is used.>`

**Headers:**

| Header | Mandatory | Description |
|--------|-----------|-------------|
| `Content-Type: <type>` | `<Yes/No>` | `<description>` |
| `Authorization: <scheme> <token>` | `<Yes/No>` | `<description>` |
| `<Other-Header>: <value>` | `<Yes/No>` | `<description>` |

**Query parameters:**

| Parameter | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| `<param1>` | `<type>` | `<Yes/No>` | `<description>` |
| `<param2>` | `<type>` | `<Yes/No>` | `<description>` |

**Path parameters:**

| Parameter | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| `<param1>` | `<type>` | `Yes` | `<description>` |

**Request body:**

```json
{
  "field1": "type_or_example_value",
  "field2": "type_or_example_value_or_null",
  "nestedObject": {
    "subField1": "type_or_example_value",
    "subField2": "type_or_example_value"
  }
}
```

**Request body constraints:**

- `field1`: `<validation constraints>`
- `field2`: `<validation constraints>`
- `nestedObject.subField1`: `<validation constraints>`

**Response codes:**

| HTTP code | Meaning | Usage condition |
|-----------|---------|-----------------|
| `200` | `OK` | `<successful retrieval/update>` |
| `201` | `Created` | `<successful creation>` |
| `204` | `No Content` | `<successful operation without body>` |
| `400` | `Bad Request` | `<invalid request or validation error>` |
| `401` | `Unauthorized` | `<missing/invalid authentication>` |
| `403` | `Forbidden` | `<insufficient authorization>` |
| `404` | `Not Found` | `<resource not found>` |
| `409` | `Conflict` | `<business or concurrency conflict>` |
| `500` | `Internal Server Error` | `<unexpected technical failure>` |

**Example response - `<HTTP code>` `<status>`:**

```json
{
  "field1": "example_value",
  "field2": "example_value",
  "nestedObject": {
    "subField": "example_value"
  }
}
```

**Example error response - `<HTTP code>` `<status>`:**

```json
{
  "error": "API_ERROR_CODE",
  "message": "Descriptive error message",
  "details": {
    "field": "additional_information"
  }
}
```

#### 4.4.2 Endpoint - `<endpoint description>`

Repeat the full endpoint structure above for each required endpoint.

### 4.5 Data model

#### 4.5.1 Table: `<table_name_1>`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `<column_1>` | `<TYPE(size)>` | `PRIMARY KEY, NOT NULL` | `<field description>` |
| `<column_2>` | `<TYPE(size)>` | `NOT NULL, DEFAULT <value>` | `<field description>` |
| `<column_3>` | `<TYPE(size)>` | `FOREIGN KEY -> <ref_table>(<ref_column>), NOT NULL` | `<field description>` |
| `<column_n>` | `<TYPE(size)>` | `<constraints>` | `<field description>` |

**Indexes:**

- `<index name or description>` on `(<column list>)` for `<reason>`.
- `<unique composite index>` on `(<column_1>, <column_2>)` for `<reason>`.

**Persistence rules:**

- `<rule concerning data generation or persistence>`
- `<rule concerning business constraints>`
- `<rule concerning transformations applied before storage>`

#### 4.5.2 Table: `<table_name_2>`

Repeat the full table structure above for each required table.

#### 4.5.3 Relationships between tables

Describe cardinalities and dependencies.

- `<table_1>.<column>` → `<table_2>.<column>`: `<relationship type and rule>`
- `<table_2>.<column>` → `<table_3>.<column>`: `<relationship type and rule>`

### 4.6 Business rules and validations

#### RG-001 - `<rule name>`

- **Description:** `<detailed description of the rule>`
- **Pattern:** `<regular expression if applicable>`
- **Length:** `<length constraints if applicable>`
- **Normalization:** `<normalization applied if applicable>`
- **Conditions:** `<conditions under which the rule applies>`
- **Failure behavior:** `<expected behavior if the rule fails>`

#### RG-002 - `<rule name>`

- **Description:** `<detailed description of the rule>`
- **Example:** `<concrete example>`
- **Conditions:** `<application conditions>`
- **Processing:** `<actions to perform>`
- **Failure behavior:** `<expected behavior if the rule fails>`

#### RG-003 - `<rule name>`

- **Description:** `<detailed description of the rule>`
- **Processing:** `<actions to perform>`
- **Failure behavior:** `<expected behavior>`

### 4.7 Error management

| HTTP code | API error code | User message | Corrective action |
|-----------|----------------|--------------|-------------------|
| `<code>` | `<TECHNICAL_ERROR_CODE>` | `<clear user-facing message>` | `<action to take>` |
| `<code>` | `<TECHNICAL_ERROR_CODE>` | `<clear user-facing message>` | `<action to take>` |
| `<code>` | `<TECHNICAL_ERROR_CODE>` | `<clear user-facing message>` | `<action to take>` |

**Retry strategy:**

- **Retry cases:** `<cases requiring retry>`
- **Maximum attempts:** `<number>`
- **Delay policy:** `<fixed / exponential backoff / jitter>`
- **Non-retryable cases:** `<cases where retry must not occur>`

**Logging:**

- **ERROR:** `<events to log as errors>`
- **WARN:** `<events to log as warnings>`
- **INFO:** `<events to log as informational events>`
- **Log format:** `<correlationId, userId, timestamp, action, resource, result, errorCode, technicalDetails>`
- **Sensitive data policy:** `<fields that must be masked or excluded from logs>`

### 4.8 Edge cases

> Pay particular attention to concurrency, security, invalid data, external-system failures and boundary values.

#### EC-001 - `<edge case description>`

- **Input:** `<input data>`
- **Processing:** `<transformation or validation applied>`
- **Expected result:** `<expected behavior>`

#### EC-002 - `<edge case description>`

- **Scenario:** `<context description>`
- **Expected result:** `<expected behavior>`
- **Justification:** `<reason for this behavior>`

#### EC-003 - `<edge case description>`

- **Scenario:** `<context description>`
- **Constraint:** `<technical or business limitation>`
- **Expected result:** `<expected behavior>`

### 4.9 Functional dependencies

| Dependency | Type | Description | Impact |
|------------|------|-------------|--------|
| `<US-ID>` | `<Prerequisite>` | `<brief description>` | `<impact if unavailable>` |
| `<US-ID>` | `<Follow-up>` | `<brief description>` | `<impact>` |
| `<US-ID>` | `<Parallel>` | `<relationship description>` | `<impact>` |

---

## 5. Non-functional requirements

| Category | Requirement | Source |
|----------|-------------|--------|
| Performance | `<endpoint/action>` must respond in less than `<value>` under `<load condition>`. | `<NFR / architecture / SLA>` |
| Performance | The system must support at least `<value>` concurrent requests or users. | `<NFR / SLA>` |
| Security | The system must enforce authentication before allowing `<action>`. | `<security policy / UC-ID>` |
| Security | The system must enforce authorization checks based on `<role/permission/ownership>`. | `<security policy / UC-ID>` |
| Security | Sensitive data must be masked in logs and error messages. | `<security policy>` |
| Reliability | The system must handle `<external system failure>` without data corruption. | `<architecture / integration constraint>` |
| Availability | The feature must remain available according to `<SLA>`. | `<SLA>` |
| Observability | The system must log `<events>` with a correlation identifier. | `<logging standard>` |
| Maintainability | Code must follow `<coding standards / architecture rules>`. | `<technical standard>` |
| Compatibility | The feature must be compatible with `<browser/device/API version>`. | `<platform requirement>` |
| Accessibility | UI components must comply with `<accessibility standard if applicable>`. | `<UX/accessibility requirement>` |

### 5.1 Technical dependencies

| Component | Role | Version |
|-----------|------|---------|
| `<technology_1>` | `<role in implementation>` | `<version if relevant>` |
| `<technology_2>` | `<role in implementation>` | `<version if relevant>` |
| `<library_n>` | `<role in implementation>` | `<version if relevant>` |

### 5.2 Performance acceptance criteria

| Metric | Acceptable threshold | Optimal threshold |
|--------|----------------------|-------------------|
| `<response time endpoint/action>` | `< value>` | `< value>` |
| `<concurrent requests>` | `<value> req/s` | `<value> req/s` |
| `<memory usage>` | `< value>` | `< value>` |
| `<database query duration>` | `< value>` | `< value>` |

### 5.3 Security acceptance criteria

- `<security test 1>`: `<description of the verification>`
- `<security test 2>`: `<description of the verification>`
- `<security test n>`: `<description of the verification>`

### 5.4 Integration acceptance criteria

| Test | Endpoint / Component | Expected result | Main assertion |
|------|----------------------|-----------------|----------------|
| `<testName1>()` | `<METHOD> <endpoint>` | `<HTTP code / result>` | `<verification description>` |
| `<testName2>()` | `<METHOD> <endpoint>` | `<HTTP code / result>` | `<verification description>` |
| `<testNameN>()` | `<METHOD> <endpoint>` | `<HTTP code / result>` | `<verification description>` |

### 5.5 Observability and auditability

- **Correlation ID:** `<required / optional / propagation rule>`
- **Audit events:** `<events that must be audited>`
- **Metrics:** `<metrics to expose>`
- **Alerts:** `<alert conditions>`
- **Dashboards:** `<dashboard impact or requirement>`

---

## 6. Domain glossary

| Term | Definition | Notes / Example |
|------|------------|-----------------|
| `<business term>` | `<definition>` | `<example or note>` |
| `<technical term>` | `<definition>` | `<example or note>` |
| `<acronym>` | `<definition>` | `<example or note>` |

---

## 7. Constraints

### 7.1 Technical constraints

- `<architecture constraint>`
- `<technology constraint>`
- `<database constraint>`
- `<API standard constraint>`
- `<deployment constraint>`

### 7.2 Business constraints

- `<business rule or policy constraint>`
- `<regulatory or compliance constraint>`
- `<operational constraint>`

### 7.3 Data constraints

- `<data format constraint>`
- `<data retention constraint>`
- `<data quality constraint>`
- `<data migration or compatibility constraint>`

### 7.4 Security constraints

- `<authentication constraint>`
- `<authorization constraint>`
- `<encryption or confidentiality constraint>`
- `<logging and masking constraint>`
- `<auditability constraint>`

### 7.5 UI / UX constraints

- `<responsive design constraint>`
- `<accessibility constraint>`
- `<browser compatibility constraint>`
- `<localization or language constraint>`

### 7.6 Concurrency constraints

- `<locking strategy>`
- `<optimistic/pessimistic concurrency rule>`
- `<conflict detection behavior>`
- `<conflict resolution behavior>`

---

## 8. Open questions

| ID | Question | Impact | Owner | Expected answer date | Status |
|----|----------|--------|-------|----------------------|--------|
| `OQ-001` | `<question>` | `<impact if unanswered>` | `<owner>` | `<YYYY-MM-DD>` | `<Open / Answered / Deferred>` |
| `OQ-002` | `<question>` | `<impact if unanswered>` | `<owner>` | `<YYYY-MM-DD>` | `<Open / Answered / Deferred>` |

---

## 9. UML sequence diagram

> This section must contain **PlantUML code only** for the diagrams.  
> The code must be directly usable in a PlantUML renderer.

### 9.1 Nominal flow

```plantuml
@startuml
title <US-ID> - <Nominal flow title>

actor <Actor> as Actor
participant "<UI Component>" as UI
participant "<API / Controller>" as API
participant "<Service>" as Service
participant "<Repository>" as Repository
database "<Database>" as DB
participant "<External System>" as External

Actor -> UI: <Action>
UI -> API: <HTTP request / command>
API -> Service: <Invoke business operation>
Service -> Repository: <Read/write data>
Repository -> DB: <SQL / persistence operation>
DB --> Repository: <Data / confirmation>
Repository --> Service: <Domain object / result>
Service -> External: <External call if applicable>
External --> Service: <External response>
Service --> API: <Service result>
API --> UI: <HTTP response>
UI --> Actor: <Display success / final state>

@enduml
```

### 9.2 Alternative or error flow

```plantuml
@startuml
title <US-ID> - <Alternative or error flow title>

actor <Actor> as Actor
participant "<UI Component>" as UI
participant "<API / Controller>" as API
participant "<Service>" as Service
participant "<Repository>" as Repository
database "<Database>" as DB

Actor -> UI: <Action>
UI -> API: <HTTP request / command>
API -> Service: <Invoke business operation>
Service -> Repository: <Read or validate data>
Repository -> DB: <Query / check>
DB --> Repository: <Error / not found / conflict>
Repository --> Service: <Failure result>
Service --> API: <Business or technical error>
API --> UI: <HTTP error response>
UI --> Actor: <Display error message / corrective action>

@enduml
```

---

## 10. Required quality checks before approval

The technical analysis is complete only if all checks below are satisfied.

### 10.1 Completeness checklist

- [ ] The User Story reference is complete and unambiguous.
- [ ] The scope clearly identifies what is in scope and out of scope.
- [ ] All actors and systems are listed.
- [ ] All use cases include preconditions, main flows, alternates and postconditions.
- [ ] All functional requirements are traceable to a source.
- [ ] All screens/interfaces are described with components, rules and navigation.
- [ ] All field validations are documented.
- [ ] All required API endpoints are documented with request, response and error examples.
- [ ] All database tables, columns, constraints, indexes and relationships are specified.
- [ ] All business rules are explicit and testable.
- [ ] All error cases include HTTP code, API error code, user message and corrective action.
- [ ] Edge cases cover concurrency, security, invalid data and external failures.
- [ ] Functional, technical and external dependencies are listed.
- [ ] Non-functional requirements are measurable.
- [ ] Performance, security and integration acceptance criteria are defined.
- [ ] PlantUML sequence diagrams are present and syntactically usable.
- [ ] Open questions are listed with owner, impact and expected answer date.

### 10.2 Agentic AI writing rules

When an AI agent generates a technical analysis using this template, it must follow these rules:

1. **Be exhaustive.** Do not omit a section because information is missing; instead, fill it with explicit placeholders or open questions.
2. **Be implementation-oriented.** Describe what must be built, where, and how it behaves.
3. **Be traceable.** Every functional and non-functional requirement must refer to a source.
4. **Be testable.** Every rule, API behavior and error case must be verifiable through tests.
5. **Be explicit.** Avoid ambiguous wording such as “handle correctly”, “as needed”, “etc.” or “should be fine”.
6. **Use concrete examples only when useful.** JSON examples, regex patterns and sample responses must add implementation value.
7. **Separate business behavior from technical behavior.** Business rules belong under requirements/rules; implementation details belong under API, data model, dependencies and constraints.
8. **Protect sensitive information.** Do not expose secrets, personal data or confidential values in examples, logs or diagrams.
9. **Document uncertainty.** If a requirement is unclear, create an open question and describe its impact.
10. **Keep each User Story self-contained.** The technical analysis must be understandable without reading another User Story, except for explicitly listed dependencies.

---

