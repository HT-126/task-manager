package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// @RestController: methods return objects that are written as JSON.
// The caller's id comes from the verified JWT ("sub" claim), never from the request body.
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    private static Long userId(Jwt token) {
        return Long.valueOf(token.getSubject());
    }

    // @Valid runs the validation rules on the body; @ResponseStatus sets 201 Created.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@AuthenticationPrincipal Jwt token, @Valid @RequestBody TaskRequest request) {
        return TaskResponse.from(service.create(userId(token), request));
    }

    // @RequestParam: reads ?status=DONE from the URL; optional. A bad value gives 400.
    @GetMapping
    public List<TaskResponse> list(@AuthenticationPrincipal Jwt token,
                                   @RequestParam(required = false) TaskStatus status) {
        return service.list(userId(token), status).stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse get(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return TaskResponse.from(service.get(userId(token), id));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
                               @Valid @RequestBody TaskRequest request) {
        return TaskResponse.from(service.update(userId(token), id, request));
    }

    // 204 No Content: success with an empty body.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        service.delete(userId(token), id);
    }
}
