# Design

## Context

The app already exposes `POST /api/tasks`, `GET /api/tasks`, and `GET /api/tasks/{id}` with layers Controller → Service → Repository, DTO records, and Bean Validation (`@NotBlank` on title). There is no global exception handler, no priority field, and `findAll()` loads every row. Persistence uses JPA with `TaskStatus` stored as STRING enums. See proposal.md for motivation; specs under `specs/tasks/` for behavior contracts.

## Goals / Non-Goals

**Goals:**

- Add `TaskPriority` and wire it through entity, create request, responses, and create path with default `MEDIUM`.
- Filter list results with optional `status` / `priority` query params via a repository query (criteria applied in SQL/JPQL).
- Centralize validation and JSON enum binding failures in one `@RestControllerAdvice` so 400 bodies name the field.
- Keep existing response fields and endpoint paths additive/backward compatible.

**Non-Goals:**

- Updating priority after create, sorting, pagination, or auth.
- Changing existing `TaskStatus` values or create/get-by-id contracts beyond adding `priority`.
- Introducing Flyway/Liquibase solely for this change (use the project's current schema update approach).
- New third-party dependencies.

## Decisions

### 1. `TaskPriority` enum stored as STRING

- **Choice**: Add `TaskPriority { LOW, MEDIUM, HIGH }` with `@Enumerated(EnumType.STRING)` on `Task`, default field value `MEDIUM`, mirroring `TaskStatus`.
- **Why**: Matches existing enum persistence; readable DB values; Jackson binds enum names for request/response.
- **Alternatives**: Ordinal storage (brittle); free-form string with custom validation (more code, weaker type safety).

### 2. Optional priority on create via nullable DTO field

- **Choice**: `CreateTaskRequest(String title, TaskPriority priority)` with `@NotBlank` on title; `priority` may be null. Service (or entity constructor) applies `MEDIUM` when null.
- **Why**: Omitted JSON property deserializes to null without custom deserializers; keeps defaulting in one place.
- **Alternatives**: Default in record compact constructor (harder to distinguish "omitted" vs explicit null); `@JsonSetter(nulls = AS_EMPTY)` (unnecessary complexity).

### 3. Database filtering via Spring Data derived or `@Query` method

- **Choice**: Repository method that accepts nullable `TaskStatus` and `TaskPriority` and filters only non-null args (e.g. JPQL with `(:status is null or t.status = :status)` and the same for priority). Service passes request params through; controller does not filter lists.
- **Why**: Satisfies the non-functional constraint that filtering happens in the DB; avoids loading all rows then streaming in memory.
- **Alternatives**: `JpaSpecificationExecutor` (more flexible but heavier for two optional equals); in-memory `stream().filter` (explicitly forbidden).

### 4. Field-named 400s via `@RestControllerAdvice`

- **Choice**: Single advice handling `MethodArgumentNotValidException` (Bean Validation) and `HttpMessageNotReadableException` (invalid enum JSON). Error body is a small consistent shape (e.g. `message` and optionally `field` / `errors`) that includes the field name (`title`, `priority`) in the message text so acceptance criteria are met.
- **Why**: Controllers stay free of error formatting; invalid priority often fails at Jackson before `@Valid`; blank title fails Bean Validation. One handler covers both paths.
- **Alternatives**: Per-controller `@ExceptionHandler` (duplication); custom deserializer only for priority (misses title naming consistency).

### 5. Backward-compatible response DTO

- **Choice**: Extend `TaskResponse` with `priority` as an additional component; leave `id`, `title`, `status` in place.
- **Why**: Additive JSON field; existing clients that ignore unknown fields keep working.

## Risks / Trade-offs

- **[Risk] Existing DB rows lack `priority` after column add** → Mitigation: non-null column with default `MEDIUM` at entity/DDL level; for H2/dev, `ddl-auto` update plus entity default; document that production migrations must backfill if using a managed schema later.
- **[Risk] Invalid query enum values (`?priority=URGENT`) also 400** → Mitigation: Same advice (or Spring's conversion failure handler) should name `priority`/`status`; cover at least create invalid priority in tests; optionally assert list query conversion errors if time allows.
- **[Risk] Overly generic error body shape** → Mitigation: Prefer a message string that literally contains the field name; keep structure minimal and consistent with spring-boot workspace rules.

## Migration Plan

1. Deploy code that adds `priority` column (default `MEDIUM`) and new API behavior.
2. Clients may start sending `priority` and using filter query params; old clients omit them and still work.
3. Rollback: revert deploy; optional column drop if schema was auto-updated (dev only). No API version bump required.

## Open Questions

None — acceptance criteria fix priority values, defaults, filter semantics, and field-named validation messages.
