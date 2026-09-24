# Tasks

## 1. Due date domain model and DTOs

- [x] 1.1 Add nullable `LocalDate dueDate` on `Task`; extend `CreateTaskRequest` and `TaskResponse` (include `dueDate` in `from`); verify with a focused unit/mapping test that unset due date maps to `null` in the response and an explicit date round-trips
- [x] 1.2 Update `TaskService.create` to persist optional `dueDate` from the request; verify with a Mockito service unit test that save receives the given date when provided and null when omitted

## 2. Invalid due date validation

- [x] 2.1 Ensure malformed `dueDate` on `POST /api/tasks` returns HTTP 400 with a message naming `dueDate` (extend or reuse `ApiExceptionHandler` / Jackson path as needed); verify with `@WebMvcTest` sending an invalid date string

## 3. Create and read due date via API

- [x] 3.1 Wire create/get/list to return `dueDate` without changing existing fields; verify with `@WebMvcTest` that create with `dueDate`, create omitting `dueDate`, get-by-id, and list responses expose the field (`null` when unset)

## 4. Clock and overdue database filtering

- [x] 4.1 Register a `Clock` bean (`Clock.systemDefaultZone()`) and inject it into `TaskService`; verify with a Mockito service test that overdue “today” is derived from the injected clock
- [x] 4.2 Extend `TaskRepository` JPQL to filter by optional `status`, `priority`, and `overdue` (strict `dueDate < today` when `overdue` is true, AND with other params); verify with `@DataJpaTest` using a fixed reference date passed into the query that overdue-only and combined filters return correct rows and tasks without `dueDate` are excluded when overdue applies
- [ ] 4.3 Wire optional `overdue` query param through `TaskController` → `TaskService` → repository (no in-memory filtering); verify with Mockito service test that repository is called with expected params and with `@WebMvcTest` that `GET /api/tasks?overdue=true` (and combined with status/priority) returns expected mocked results
