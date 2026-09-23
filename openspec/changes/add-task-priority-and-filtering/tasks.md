# Tasks

## 1. Priority domain model and DTOs

- [x] 1.1 Add `TaskPriority` enum (`LOW`, `MEDIUM`, `HIGH`) and persist it on `Task` with `@Enumerated(STRING)`, default `MEDIUM`; extend `CreateTaskRequest` (optional priority) and `TaskResponse` (include priority); verify with a focused unit/mapping test that `TaskResponse.from` includes priority and omitted create priority maps to `MEDIUM`
- [x] 1.2 Update `TaskService.create` to apply default `MEDIUM` when request priority is null and save with the chosen priority; verify with a Mockito service unit test that save receives `HIGH` when requested and `MEDIUM` when priority is null

## 2. Field-named validation errors

- [x] 2.1 Add a `@RestControllerAdvice` that returns HTTP 400 with a consistent error body whose message names the field for Bean Validation failures (`title`) and unreadable JSON / invalid enum values (`priority`); verify with `@WebMvcTest` that blank title → 400 naming `title`, and invalid priority → 400 naming `priority`

## 3. Create and read priority via API

- [x] 3.1 Ensure create/get/list paths return `priority` without changing existing fields; verify with `@WebMvcTest` (and service mocks as needed) that `POST` with `priority: HIGH` returns 201 with `HIGH`, and `POST` omitting priority returns 201 with `MEDIUM`

## 4. Database filtering

- [ ] 4.1 Add a `TaskRepository` query method that filters by optional `status` and `priority` in JPQL/SQL (`null` param = no filter on that field); verify with `@DataJpaTest` that seeding mixed rows and querying `OPEN` + `HIGH` returns only matches, and omitting a param does not filter that field
- [ ] 4.2 Wire optional `status` and `priority` query params through `TaskController` → `TaskService` → repository (no in-memory filtering); verify with a Mockito service test that the repository filter method is invoked with the given params, and with `@WebMvcTest` that `GET /api/tasks?status=OPEN&priority=HIGH` returns only the mocked matching tasks
