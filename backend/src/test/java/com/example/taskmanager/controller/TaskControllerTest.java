package com.example.taskmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanager.config.CorsConfig;
import com.example.taskmanager.config.SecurityConfig;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.RevokedTokenRepository;
import com.example.taskmanager.service.TaskService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

// @WebMvcTest: starts only the web layer; the service is replaced by a mock.
// jwt() simulates a logged-in caller (user id 1) without a real token.
@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, CorsConfig.class, GlobalExceptionHandler.class})
class TaskControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    TaskService service;

    @MockitoBean
    RevokedTokenRepository revokedTokens;

    private static RequestPostProcessor user() {
        return jwt().jwt(j -> j.subject("1"));
    }

    @Test
    void requestWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }

    @Test
    void postCreatesTask() throws Exception {
        Task saved = new Task();
        saved.setTitle("write plan");
        given(service.create(eq(1L), any())).willReturn(saved);

        mvc.perform(post("/api/tasks").with(user()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"write plan\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("write plan"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void postWithoutTitleReturns400() throws Exception {
        mvc.perform(post("/api/tasks").with(user()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"no title\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    void getUnknownIdReturns404() throws Exception {
        given(service.get(1L, 99L)).willThrow(new TaskNotFoundException(99L));

        mvc.perform(get("/api/tasks/99").with(user())).andExpect(status().isNotFound());
    }

    @Test
    void listPassesStatusFilterToService() throws Exception {
        Task done = new Task();
        done.setTitle("finished");
        done.setStatus(TaskStatus.DONE);
        given(service.list(1L, TaskStatus.DONE)).willReturn(List.of(done));

        mvc.perform(get("/api/tasks").with(user()).param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("finished"))
                .andExpect(jsonPath("$[0].status").value("DONE"));
    }

    @Test
    void listWithUnknownStatusReturns400() throws Exception {
        mvc.perform(get("/api/tasks").with(user()).param("status", "BOGUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putUpdatesTask() throws Exception {
        Task updated = new Task();
        updated.setTitle("new title");
        updated.setStatus(TaskStatus.DOING);
        given(service.update(eq(1L), eq(1L), any())).willReturn(updated);

        mvc.perform(put("/api/tasks/1").with(user()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"new title\",\"status\":\"DOING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("new title"))
                .andExpect(jsonPath("$.status").value("DOING"));
    }

    @Test
    void putWithoutTitleReturns400() throws Exception {
        mvc.perform(put("/api/tasks/1").with(user()).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putUnknownIdReturns404() throws Exception {
        given(service.update(eq(1L), eq(99L), any())).willThrow(new TaskNotFoundException(99L));

        mvc.perform(put("/api/tasks/99").with(user()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/tasks/1").with(user())).andExpect(status().isNoContent());
        verify(service).delete(1L, 1L);
    }

    @Test
    void deleteUnknownIdReturns404() throws Exception {
        willThrow(new TaskNotFoundException(99L)).given(service).delete(1L, 99L);

        mvc.perform(delete("/api/tasks/99").with(user())).andExpect(status().isNotFound());
    }

    @Test
    void corsAllowsDevServerOrigin() throws Exception {
        given(service.list(eq(1L), any())).willReturn(List.of());

        mvc.perform(get("/api/tasks").with(user()).header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
