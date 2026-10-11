package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;

// @Service: marks a business-logic bean; Spring creates it and injects the repository.
@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        return repository.save(task);
    }

    // status == null means "no filter": return every task.
    public List<Task> list(TaskStatus status) {
        return status == null ? repository.findAll() : repository.findByStatus(status);
    }

    public Task get(Long id) {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    // Replaces title/description/dueDate; a null status keeps the current one.
    public Task update(Long id, TaskRequest request) {
        Task task = get(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        return repository.save(task);
    }

    public void delete(Long id) {
        repository.delete(get(id));
    }
}
