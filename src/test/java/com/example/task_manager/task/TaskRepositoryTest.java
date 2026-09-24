package com.example.task_manager.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void seed() {
        repository.save(task("open-high", TaskStatus.OPEN, TaskPriority.HIGH));
        repository.save(task("open-low", TaskStatus.OPEN, TaskPriority.LOW));
        repository.save(task("done-high", TaskStatus.DONE, TaskPriority.HIGH));
        repository.save(task("done-medium", TaskStatus.DONE, TaskPriority.MEDIUM));
    }

    @Test
    void findByStatusAndPriorityReturnsOnlyMatches() {
        List<Task> results = repository.findByStatusAndPriority(TaskStatus.OPEN, TaskPriority.HIGH);

        assertThat(results).extracting(Task::getTitle).containsExactly("open-high");
    }

    @Test
    void omittingStatusDoesNotFilterOnStatus() {
        List<Task> results = repository.findByStatusAndPriority(null, TaskPriority.HIGH);

        assertThat(results).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("open-high", "done-high");
    }

    @Test
    void omittingPriorityDoesNotFilterOnPriority() {
        List<Task> results = repository.findByStatusAndPriority(TaskStatus.OPEN, null);

        assertThat(results).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("open-high", "open-low");
    }

    @Test
    void omittingBothReturnsAll() {
        List<Task> results = repository.findByStatusAndPriority(null, null);

        assertThat(results).hasSize(4);
    }

    private static Task task(String title, TaskStatus status, TaskPriority priority) {
        Task task = new Task(title);
        task.setStatus(status);
        task.setPriority(priority);
        return task;
    }
}
