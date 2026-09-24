package com.example.task_manager.task;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateTaskRequest(@NotBlank String title, TaskPriority priority, LocalDate dueDate) {
}
