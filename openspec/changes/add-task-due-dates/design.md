# Design

## Context

The task API already supports create, get-by-id, and list with optional `status` / `priority` filters implemented in JPQL on `TaskRepository`, DTO records for requests and responses, Bean Validation on title, and `ApiExceptionHandler` for field-named 400 responses on validation and invalid enum JSON. There is no `dueDate` field and no injectable clock. See proposal.md for motivation; delta specs under `specs/tasks/` for behavior contracts.

## Goals / Non-Goals

**Goals:**

- Persist optional `dueDate` as a calendar date on `Task` and expose it on create and all read responses (`null` when unset).
- Extend list filtering with optional `overdue=true`, combined with existing filters via AND, evaluated in the repository query using “today” from an injected `Clock`.
- Ensure invalid `dueDate` on create yields 400 with message naming `dueDate` (Jackson parse failures and/or Bean Validation, consistent with existing handler patterns).
- Register a `Clock` bean for production and inject it into `TaskService` (or a small helper used only for overdue boundary) for deterministic tests.

**Non-Goals:**

- Updating `dueDate` after create, time-of-day deadlines, time zones per user, or reminders/notifications.
- Changing priority/status semantics or pagination.
- New migration tooling beyond the project's current JPA/DDL approach.
- New third-party dependencies.

## Decisions

### 1. Store `dueDate` as `LocalDate` with nullable column

- **Choice**: Add `LocalDate dueDate` on `Task` with nullable JPA column; JSON as ISO-8601 date (`yyyy-MM-dd`) via Jackson default for `LocalDate`.
- **Why**: Matches “ISO date” requirement; no time component avoids timezone ambiguity for due dates; null means unset.
- **Alternatives**: `Instant`/`OffsetDateTime` (overkill); string column (weaker typing).

### 2. Optional `dueDate` on `CreateTaskRequest`

- **Choice**: Add nullable `LocalDate dueDate` to the create record; service sets entity field only when non-null (or explicitly allows null on entity).
- **Why**: Omitted JSON property deserializes to null; backward compatible for clients that do not send the field.
- **Alternatives**: `Optional` in record (non-idiomatic for Jackson DTOs).

### 3. Extend repository JPQL with nullable overdue flag

- **Choice**: Extend `findByStatusAndPriority` (rename if needed) to accept a nullable `Boolean overdue` and `LocalDate today` computed once in the service from `clock.instant().atZone(clock.getZone()).toLocalDate()`. When overdue applies, JPQL requires `t.status <> DONE` in addition to `t.dueDate is not null and t.dueDate < :today`, e.g. `(:overdue is null or :overdue = false or (t.status <> DONE and t.dueDate is not null and t.dueDate < :today))` (exact spelling in implementation).
- **Why**: Keeps filtering in the database; AND semantics align with existing null-parameter pattern for status/priority.
- **Alternatives**: Separate query methods (duplication); in-memory filter (forbidden by spec).

### 4. `overdue` query parameter as boolean, only `true` activates filter

- **Choice**: Controller accepts `@RequestParam(required = false) Boolean overdue`; service passes `Boolean.TRUE.equals(overdue)` into query logic so absent/`false` does not filter.
- **Why**: Clear contract; avoids treating invalid strings as filter without extra work (Spring may 400 on bad boolean—acceptable, optional test).
- **Alternatives**: String `"true"`/`"false"` parsing in service (more code).

### 5. Injectable `Clock` bean

- **Choice**: `@Bean Clock clock() { return Clock.systemDefaultZone(); }` in application configuration (or existing config class); constructor-inject into `TaskService`.
- **Why**: Fixed “today” in unit/integration tests via `Clock.fixed(...)`; production uses system default zone consistently with overdue boundary.
- **Alternatives**: Static `LocalDate.now()` (untestable); passing date only in tests via package-private hack (worse).

### 6. Invalid date errors via existing `@RestControllerAdvice`

- **Choice**: Rely on `HttpMessageNotReadableException` from Jackson for malformed dates; ensure `resolveField` returns `dueDate` when path points to that property (same pattern as `priority`). Add Bean Validation only if a custom constraint is needed—prefer Jackson + existing handler first.
- **Why**: Matches priority invalid-value path; single error shape.
- **Alternatives**: Custom deserializer (only if handler does not name field reliably).

### 7. Backward-compatible `TaskResponse`

- **Choice**: Add `LocalDate dueDate` component to `TaskResponse.from(Task)`; JSON serializes as date or null.
- **Why**: Additive field; existing clients ignore unknown/nullable fields.

## Risks / Trade-offs

- **[Risk] Zone for “today” differs from client expectation** → Mitigation: Document that overdue uses the JVM default zone via `Clock.systemDefaultZone()`; tests pin zone on fixed clock.
- **[Risk] Tasks due “today” excluded from overdue** → Mitigation: Spec uses strict `< today`; tests assert boundary with fixed clock.
- **[Risk] Repository method signature churn** → Mitigation: Rename method to reflect filters (e.g. `findByFilters`) in one change; update service and `@DataJpaTest` together.

## Migration Plan

1. Deploy code adding nullable `dueDate` column and API fields/params.
2. Existing clients unchanged; new clients may send `dueDate` and use `overdue=true`.
3. Rollback: revert deploy; optional column drop in dev if schema auto-updated.

## Open Questions

None — acceptance criteria fix date format, response shape, overdue AND semantics, DB filtering, clock injection, and tests.
