package com.example.task_manager.task;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskRepository repository;
    private final Clock clock;

    public TaskService(TaskRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    LocalDate todayForOverdue() {
        return clock.instant().atZone(clock.getZone()).toLocalDate();
    }

    public TaskResponse create(CreateTaskRequest request) {
        Task task = new Task(request.title());
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setDueDate(request.dueDate());
        Task saved = repository.save(task);
        return TaskResponse.from(saved);
    }

    public List<TaskResponse> findAll(TaskStatus status, TaskPriority priority) {
        return repository.findByStatusAndPriority(status, priority).stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse findById(Long id) {
        return repository.findById(id)
                .map(TaskResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }
}
