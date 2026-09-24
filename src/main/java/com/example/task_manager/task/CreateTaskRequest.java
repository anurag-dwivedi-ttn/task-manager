package com.example.task_manager.task;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(@NotBlank String title, TaskPriority priority) {
}
