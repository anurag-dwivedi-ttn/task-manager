# Proposal

## Why

Clients need to schedule work with deadlines and quickly surface tasks that are past due. Today tasks have no date field and list filtering cannot express overdue work, which limits planning and reminder-style integrations.

## What Changes

- Add an optional `dueDate` field on task create (ISO calendar date, e.g. `2026-09-24`); omitting it leaves the task without a due date.
- Include `dueDate` on task responses from create, get-by-id, and list (`null` when unset).
- Reject invalid `dueDate` values on create with HTTP 400 and an error message that names the `dueDate` field.
- Extend `GET /api/tasks` with optional query parameter `overdue=true` to return only tasks whose `dueDate` is strictly before today; combine with existing `status` and `priority` filters using logical AND. Overdue filtering runs in the database query.
- Use an injectable `java.time.Clock` for “today” so overdue logic is testable and timezone-stable.
- Cover all new behavior with automated tests. Existing response fields and endpoints remain backward compatible (additive only).

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `tasks`: Optional due date on create and responses, field-named validation for invalid dates, and optional overdue list filtering combined with status/priority filters.

## Impact

- **API**: `POST /api/tasks` accepts optional `dueDate`; responses gain nullable `dueDate`; `GET /api/tasks` gains optional `overdue` query param. Error bodies for bad dates name `dueDate`.
- **Code**: `Task` entity, DTOs (`CreateTaskRequest`, `TaskResponse`), `TaskRepository` (extend JPQL filter), `TaskService`, `TaskController`, `ApiExceptionHandler` (if date parsing needs explicit handling), and a `Clock` bean wired into the service layer for overdue comparisons.
- **Persistence**: Nullable `dueDate` column (ISO date stored as `LocalDate` / SQL `DATE`).
- **Tests**: Controller, service, and repository tests for create/read mapping, invalid date 400, overdue filter semantics (including AND with status/priority), and clock injection.
