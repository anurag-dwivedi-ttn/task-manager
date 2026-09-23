package com.example.task_manager.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    @Test
    void createSavesHighPriorityWhenRequested() {
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = service.create(new CreateTaskRequest("Urgent", TaskPriority.HIGH));

        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
        verify(repository).save(argThat(task ->
                "Urgent".equals(task.getTitle()) && task.getPriority() == TaskPriority.HIGH));
    }

    @Test
    void createSavesMediumPriorityWhenPriorityIsNull() {
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = service.create(new CreateTaskRequest("Routine", null));

        assertThat(response.priority()).isEqualTo(TaskPriority.MEDIUM);
        verify(repository).save(argThat(task ->
                "Routine".equals(task.getTitle()) && task.getPriority() == TaskPriority.MEDIUM));
    }
}
