# Proposal

## Why

Clients need to distinguish urgent work from routine tasks and narrow list results by status and priority. Validation failures today do not consistently name the offending field, which makes client error handling harder than it should be.

## What Changes

- Add an optional `priority` field on task create (`LOW`, `MEDIUM`, `HIGH`), defaulting to `MEDIUM` when omitted.
- Include `priority` on task responses (create, get-by-id, and list) while keeping existing fields unchanged.
- Extend `GET /api/tasks` with optional `status` and `priority` query parameters; omitted parameters do not filter that field. Filtering runs in the database query, not in memory.
- Return HTTP 400 for invalid priority values and blank titles, with error messages that name the offending field (`priority`, `title`).
- Cover all new behavior with automated tests.

## Capabilities

### New Capabilities

- `tasks`: Task create/list/get API behavior for priority, optional list filtering by status and priority, and field-named validation errors.

### Modified Capabilities

- (none — no existing specs under `openspec/specs/`)

## Impact

- **API**: `POST /api/tasks` request/response gains optional `priority`; `GET /api/tasks` gains optional `status` and `priority` query params; error bodies become field-aware for validation failures. Existing endpoints and response fields remain backward compatible (additive only).
- **Code**: `Task` entity, DTOs (`CreateTaskRequest`, `TaskResponse`), `TaskRepository` (query methods), `TaskService`, `TaskController`, plus a global exception handler for consistent 400 responses. New `TaskPriority` enum mirroring the existing `TaskStatus` pattern.
- **Persistence**: New non-null `priority` column; schema update via the project's current JPA/DDL approach.
- **Tests**: Controller (`@WebMvcTest`), service (Mockito), and repository (`@DataJpaTest`) coverage for priority defaults, invalid values, blank title, and DB-level filtering.
- **Dependencies**: No new libraries; reuse `spring-boot-starter-validation` and existing JPA stack.
