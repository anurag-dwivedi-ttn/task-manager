# Spec Delta

## Purpose

Defines task API behavior for priority on create and responses, optional list filtering by status and priority, and field-named validation error responses.

## ADDED Requirements

### Requirement: Task priority on create and responses
The system SHALL persist a task priority that is one of `LOW`, `MEDIUM`, or `HIGH`. When a client creates a task without a `priority` field, the system MUST assign `MEDIUM`. Task responses from create, get-by-id, and list MUST include the task's `priority` while continuing to include existing fields (`id`, `title`, `status`).

#### Scenario: Create with explicit HIGH priority
- **WHEN** a client sends a valid `POST /api/tasks` with `priority` set to `HIGH`
- **THEN** the response status is `201` and the response body includes `priority` equal to `HIGH`

#### Scenario: Create without priority defaults to MEDIUM
- **WHEN** a client sends a valid `POST /api/tasks` that omits the `priority` field
- **THEN** the created task has `priority` equal to `MEDIUM` in the response

#### Scenario: Get and list include priority
- **WHEN** a client retrieves a task via `GET /api/tasks/{id}` or lists tasks via `GET /api/tasks`
- **THEN** each returned task includes a `priority` field with value `LOW`, `MEDIUM`, or `HIGH`

### Requirement: Validate priority and title with field-named errors
The system SHALL reject invalid create requests with HTTP `400`. Error messages for validation failures MUST name the offending field (`priority` or `title`).

#### Scenario: Invalid priority value
- **WHEN** a client sends `POST /api/tasks` with a `priority` value that is not `LOW`, `MEDIUM`, or `HIGH`
- **THEN** the response status is `400` and the error message names the `priority` field

#### Scenario: Blank title
- **WHEN** a client sends `POST /api/tasks` with a blank `title`
- **THEN** the response status is `400` and the error message names the `title` field

### Requirement: Filter tasks by status and priority
The system SHALL allow clients to filter `GET /api/tasks` by optional query parameters `status` and `priority`. An omitted parameter MUST mean no filtering on that field. Filtering MUST be applied in the database query, not by loading all tasks and filtering in application memory. Combining both parameters MUST return only tasks that match both criteria (logical AND).

#### Scenario: Filter by status and priority
- **WHEN** a client sends `GET /api/tasks?status=OPEN&priority=HIGH`
- **THEN** the response includes only tasks whose status is `OPEN` and whose priority is `HIGH`

#### Scenario: Omit a filter parameter
- **WHEN** a client sends `GET /api/tasks` with only `status` set, or only `priority` set, or neither set
- **THEN** the system does not filter on any omitted field and returns all tasks that match the provided filters (or all tasks when both are omitted)

#### Scenario: Filtering is applied in the database
- **WHEN** a client requests a filtered task list
- **THEN** the matching criteria are applied in the database query rather than by filtering a full in-memory list of all tasks
