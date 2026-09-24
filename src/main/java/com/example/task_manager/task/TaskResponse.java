package com.example.task_manager.task;

import java.time.LocalDate;

public record TaskResponse(Long id, String title, TaskStatus status, TaskPriority priority, LocalDate dueDate) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate());
    }
}
