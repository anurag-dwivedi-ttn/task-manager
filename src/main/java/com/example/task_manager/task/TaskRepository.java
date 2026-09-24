package com.example.task_manager.task;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            select t from Task t
            where (:status is null or t.status = :status)
              and (:priority is null or t.priority = :priority)
              and (:overdue is null or :overdue = false
                   or (t.status <> com.example.task_manager.task.TaskStatus.DONE
                       and t.dueDate is not null and t.dueDate < :today))
            """)
    List<Task> findByFilters(
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("overdue") Boolean overdue,
            @Param("today") LocalDate today);
}
