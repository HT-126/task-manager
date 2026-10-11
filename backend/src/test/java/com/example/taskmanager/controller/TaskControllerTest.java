package com.example.taskmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import java.util.List;
import com.example.taskmanager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// @WebMvcTest: starts only the web layer; the service is replaced by a mock.
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    TaskService service;

    @Test
    void postCreatesTask() throws Exception {
        Task saved = new Task();
        saved.setTitle("write plan");
        given(service.create(any())).willReturn(saved);

        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"write plan\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("write plan"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void postWithoutTitleReturns400() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"no title\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownIdReturns404() throws Exception {
        given(service.get(99L)).willThrow(new TaskNotFoundException(99L));

        mvc.perform(get("/api/tasks/99")).andExpect(status().isNotFound());
    }

    @Test
    void listPassesStatusFilterToService() throws Exception {
        Task done = new Task();
        done.setTitle("finished");
        done.setStatus(TaskStatus.DONE);
        given(service.list(TaskStatus.DONE)).willReturn(List.of(done));

        mvc.perform(get("/api/tasks").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("finished"))
                .andExpect(jsonPath("$[0].status").value("DONE"));
    }

    @Test
    void listWithUnknownStatusReturns400() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "BOGUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putUpdatesTask() throws Exception {
        Task updated = new Task();
        updated.setTitle("new title");
        updated.setStatus(TaskStatus.DOING);
        given(service.update(eq(1L), any())).willReturn(updated);

        mvc.perform(put("/api/tasks/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"new title\",\"status\":\"DOING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("new title"))
                .andExpect(jsonPath("$.status").value("DOING"));
    }

    @Test
    void putWithoutTitleReturns400() throws Exception {
        mvc.perform(put("/api/tasks/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putUnknownIdReturns404() throws Exception {
        given(service.update(eq(99L), any())).willThrow(new TaskNotFoundException(99L));

        mvc.perform(put("/api/tasks/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/tasks/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }

    @Test
    void deleteUnknownIdReturns404() throws Exception {
        willThrow(new TaskNotFoundException(99L)).given(service).delete(99L);

        mvc.perform(delete("/api/tasks/99")).andExpect(status().isNotFound());
    }
}
