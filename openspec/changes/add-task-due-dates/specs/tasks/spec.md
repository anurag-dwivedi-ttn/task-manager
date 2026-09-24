# Spec Delta

## ADDED Requirements

### Requirement: Task due date on create and responses
The system SHALL accept an optional `dueDate` on task create as an ISO calendar date (e.g. `2026-09-24`). When a client omits `dueDate`, the task MUST have no due date. Task responses from create, get-by-id, and list MUST include a `dueDate` field whose value is the stored date or `null` when unset, while continuing to include existing fields (`id`, `title`, `status`, `priority`).

#### Scenario: Create with explicit due date
- **WHEN** a client sends a valid `POST /api/tasks` with `dueDate` set to `2026-09-24`
- **THEN** the response status is `201` and the response body includes `dueDate` equal to `2026-09-24`

#### Scenario: Create without due date
- **WHEN** a client sends a valid `POST /api/tasks` that omits the `dueDate` field
- **THEN** the response includes `dueDate` equal to `null`

#### Scenario: Get and list include due date
- **WHEN** a client retrieves a task via `GET /api/tasks/{id}` or lists tasks via `GET /api/tasks`
- **THEN** each returned task includes a `dueDate` field (ISO date string or `null`)

### Requirement: Validate due date with field-named errors
The system SHALL reject create requests with an invalid `dueDate` with HTTP `400`. The error message MUST name the `dueDate` field.

#### Scenario: Invalid due date value
- **WHEN** a client sends `POST /api/tasks` with a `dueDate` value that is not a valid ISO calendar date
- **THEN** the response status is `400` and the error message names the `dueDate` field

## MODIFIED Requirements

### Requirement: Filter tasks by status and priority
The system SHALL allow clients to filter `GET /api/tasks` by optional query parameters `status`, `priority`, and `overdue`. An omitted `status` or `priority` parameter MUST mean no filtering on that field. When `overdue` is exactly `true`, the system MUST return only tasks whose `dueDate` is strictly before the current calendar date according to the system's configured clock and whose `status` is not `DONE`. Tasks with status `DONE` MUST never be treated as overdue regardless of `dueDate`. When `overdue` is omitted or not `true`, the system MUST NOT filter by overdue. Combining any provided parameters MUST return only tasks that match all provided criteria (logical AND). Filtering MUST be applied in the database query, not by loading all tasks and filtering in application memory.

#### Scenario: Filter by status and priority
- **WHEN** a client sends `GET /api/tasks?status=OPEN&priority=HIGH`
- **THEN** the response includes only tasks whose status is `OPEN` and whose priority is `HIGH`

#### Scenario: Omit a filter parameter
- **WHEN** a client sends `GET /api/tasks` with only some of `status`, `priority`, and `overdue` set, or none of them set
- **THEN** the system does not filter on any omitted field and returns all tasks that match the provided filters (or all tasks when all are omitted)

#### Scenario: Filter overdue tasks
- **WHEN** a client sends `GET /api/tasks?overdue=true` and the system clock's current date is `2026-09-24`
- **THEN** the response includes only tasks whose `dueDate` is before `2026-09-24`, whose `status` is not `DONE`, and excludes tasks with no `dueDate` or with `dueDate` on or after `2026-09-24`

#### Scenario: Completed tasks are never overdue
- **WHEN** a task has status `DONE` and `dueDate` strictly before the system clock's current date
- **AND** a client sends `GET /api/tasks?overdue=true`
- **THEN** that task is not included in the response

#### Scenario: Combine overdue with status and priority
- **WHEN** a client sends `GET /api/tasks?overdue=true&status=OPEN&priority=HIGH`
- **THEN** the response includes only tasks that are overdue per the system clock, have status `OPEN`, and have priority `HIGH`

#### Scenario: Filtering is applied in the database
- **WHEN** a client requests a filtered task list including `overdue=true` and/or `status` and/or `priority`
- **THEN** the matching criteria are applied in the database query rather than by filtering a full in-memory list of all tasks
