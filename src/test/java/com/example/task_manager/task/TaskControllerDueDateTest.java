package com.example.task_manager.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TaskController.class)
class TaskControllerDueDateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createWithDueDateReturns201WithDueDate() throws Exception {
        LocalDate due = LocalDate.of(2026, 9, 24);
        when(taskService.create(any(CreateTaskRequest.class)))
                .thenReturn(new TaskResponse(1L, "Ship", TaskStatus.OPEN, TaskPriority.MEDIUM, due));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Ship\",\"dueDate\":\"2026-09-24\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").value("2026-09-24"));
    }

    @Test
    void createOmittingDueDateReturnsNullDueDate() throws Exception {
        when(taskService.create(any(CreateTaskRequest.class)))
                .thenReturn(new TaskResponse(2L, "Routine", TaskStatus.OPEN, TaskPriority.MEDIUM, null));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Routine\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").doesNotExist());
    }

    @Test
    void getAndListExposeDueDate() throws Exception {
        LocalDate due = LocalDate.of(2026, 9, 24);
        TaskResponse response = new TaskResponse(3L, "Listed", TaskStatus.OPEN, TaskPriority.LOW, due);
        when(taskService.findById(3L)).thenReturn(response);
        when(taskService.findAll(isNull(), isNull())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/tasks/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value("2026-09-24"));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dueDate").value("2026-09-24"));
    }

    @Test
    void getAndListExposeNullDueDateWhenUnset() throws Exception {
        TaskResponse response = new TaskResponse(4L, "No date", TaskStatus.OPEN, TaskPriority.MEDIUM, null);
        when(taskService.findById(4L)).thenReturn(response);
        when(taskService.findAll(isNull(), isNull())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/tasks/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").doesNotExist());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dueDate").doesNotExist());
    }
}
