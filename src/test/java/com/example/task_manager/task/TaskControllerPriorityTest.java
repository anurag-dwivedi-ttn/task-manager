package com.example.task_manager.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TaskController.class)
class TaskControllerPriorityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createWithHighPriorityReturns201WithHigh() throws Exception {
        when(taskService.create(any(CreateTaskRequest.class)))
                .thenReturn(new TaskResponse(1L, "Ship", TaskStatus.OPEN, TaskPriority.HIGH));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Ship\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Ship"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void createOmittingPriorityReturns201WithMedium() throws Exception {
        when(taskService.create(any(CreateTaskRequest.class)))
                .thenReturn(new TaskResponse(2L, "Routine", TaskStatus.OPEN, TaskPriority.MEDIUM));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Routine\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Routine"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    void getAndListIncludePriority() throws Exception {
        TaskResponse response = new TaskResponse(3L, "Listed", TaskStatus.OPEN, TaskPriority.LOW);
        when(taskService.findById(3L)).thenReturn(response);
        when(taskService.findAll(isNull(), isNull())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/tasks/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.title").value("Listed"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("LOW"))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].title").value("Listed"))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void listFiltersByStatusAndPriority() throws Exception {
        when(taskService.findAll(eq(TaskStatus.OPEN), eq(TaskPriority.HIGH)))
                .thenReturn(List.of(new TaskResponse(4L, "Urgent open", TaskStatus.OPEN, TaskPriority.HIGH)));

        mockMvc.perform(get("/api/tasks")
                        .param("status", "OPEN")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Urgent open"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }
}
