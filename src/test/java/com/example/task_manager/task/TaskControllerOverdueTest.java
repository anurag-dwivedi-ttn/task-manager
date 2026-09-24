package com.example.task_manager.task;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TaskController.class)
class TaskControllerOverdueTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void listWithOverdueTrueReturnsFilteredResults() throws Exception {
        LocalDate past = LocalDate.of(2026, 9, 20);
        when(taskService.findAll(isNull(), isNull(), eq(true)))
                .thenReturn(List.of(new TaskResponse(
                        1L, "Late task", TaskStatus.OPEN, TaskPriority.MEDIUM, past)));

        mockMvc.perform(get("/api/tasks").param("overdue", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Late task"))
                .andExpect(jsonPath("$[0].dueDate").value("2026-09-20"));
    }

    @Test
    void listWithOverdueAndStatusAndPriority() throws Exception {
        when(taskService.findAll(eq(TaskStatus.OPEN), eq(TaskPriority.HIGH), eq(true)))
                .thenReturn(List.of(new TaskResponse(
                        2L, "Urgent overdue", TaskStatus.OPEN, TaskPriority.HIGH, LocalDate.of(2026, 9, 1))));

        mockMvc.perform(get("/api/tasks")
                        .param("overdue", "true")
                        .param("status", "OPEN")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Urgent overdue"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }
}
