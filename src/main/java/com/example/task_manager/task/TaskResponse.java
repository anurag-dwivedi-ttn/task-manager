package com.example.task_manager.task;

public record TaskResponse(Long id, String title, TaskStatus status, TaskPriority priority) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getStatus(), task.getPriority());
    }
}
