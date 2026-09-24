package com.example.task_manager.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class TaskRepositoryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 24);

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void seed() {
        repository.save(task("open-high", TaskStatus.OPEN, TaskPriority.HIGH, null));
        repository.save(task("open-low", TaskStatus.OPEN, TaskPriority.LOW, null));
        repository.save(task("done-high", TaskStatus.DONE, TaskPriority.HIGH, null));
        repository.save(task("done-medium", TaskStatus.DONE, TaskPriority.MEDIUM, null));
    }

    @Test
    void findByFiltersReturnsOnlyMatchesForStatusAndPriority() {
        List<Task> results = repository.findByFilters(TaskStatus.OPEN, TaskPriority.HIGH, null, TODAY);

        assertThat(results).extracting(Task::getTitle).containsExactly("open-high");
    }

    @Test
    void omittingStatusDoesNotFilterOnStatus() {
        List<Task> results = repository.findByFilters(null, TaskPriority.HIGH, null, TODAY);

        assertThat(results).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("open-high", "done-high");
    }

    @Test
    void omittingPriorityDoesNotFilterOnPriority() {
        List<Task> results = repository.findByFilters(TaskStatus.OPEN, null, null, TODAY);

        assertThat(results).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("open-high", "open-low");
    }

    @Test
    void omittingBothReturnsAll() {
        List<Task> results = repository.findByFilters(null, null, null, TODAY);

        assertThat(results).hasSize(4);
    }

    @Test
    void overdueOnlyReturnsTasksWithDueDateStrictlyBeforeToday() {
        repository.deleteAll();
        repository.save(task("past", TaskStatus.OPEN, TaskPriority.HIGH, LocalDate.of(2026, 9, 23)));
        repository.save(task("today", TaskStatus.OPEN, TaskPriority.HIGH, LocalDate.of(2026, 9, 24)));
        repository.save(task("future", TaskStatus.OPEN, TaskPriority.HIGH, LocalDate.of(2026, 9, 25)));
        repository.save(task("no-date", TaskStatus.OPEN, TaskPriority.HIGH, null));

        List<Task> results = repository.findByFilters(null, null, true, TODAY);

        assertThat(results).extracting(Task::getTitle).containsExactly("past");
    }

    @Test
    void overdueCombinedWithStatusAndPriority() {
        repository.deleteAll();
        repository.save(task("overdue-open-high", TaskStatus.OPEN, TaskPriority.HIGH, LocalDate.of(2026, 9, 20)));
        repository.save(task("overdue-done-high", TaskStatus.DONE, TaskPriority.HIGH, LocalDate.of(2026, 9, 20)));
        repository.save(task("open-high-future", TaskStatus.OPEN, TaskPriority.HIGH, LocalDate.of(2026, 10, 1)));
        repository.save(task("open-low-past", TaskStatus.OPEN, TaskPriority.LOW, LocalDate.of(2026, 9, 20)));

        List<Task> results = repository.findByFilters(TaskStatus.OPEN, TaskPriority.HIGH, true, TODAY);

        assertThat(results).extracting(Task::getTitle).containsExactly("overdue-open-high");
    }

    private static Task task(String title, TaskStatus status, TaskPriority priority, LocalDate dueDate) {
        Task task = new Task(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        return task;
    }
}
