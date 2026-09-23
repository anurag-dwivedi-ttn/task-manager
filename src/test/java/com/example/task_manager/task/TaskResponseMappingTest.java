package com.example.task_manager.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TaskResponseMappingTest {

    @Test
    void fromIncludesPriority() {
        Task task = new Task("Ship feature");
        task.setPriority(TaskPriority.HIGH);

        TaskResponse response = TaskResponse.from(task);

        assertThat(response.title()).isEqualTo("Ship feature");
        assertThat(response.status()).isEqualTo(TaskStatus.OPEN);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void omittedCreatePriorityMapsToMedium() {
        CreateTaskRequest request = new CreateTaskRequest("Ship feature", null);

        Task task = new Task(request.title());
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        assertThat(request.priority()).isNull();
        assertThat(task.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(TaskResponse.from(task).priority()).isEqualTo(TaskPriority.MEDIUM);
    }
}
