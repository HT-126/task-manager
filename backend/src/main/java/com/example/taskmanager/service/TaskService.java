package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;

// @Service: marks a business-logic bean; Spring creates it and injects the repository.
// Every method takes the caller's user id. A task owned by someone else is reported as
// "not found" (404), so its existence is not revealed.
@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(Long ownerId, TaskRequest request) {
        Task task = new Task();
        task.setOwnerId(ownerId);
        apply(task, request);
        return repository.save(task);
    }

    // status == null means "no filter": return all of the user's tasks.
    public List<Task> list(Long ownerId, TaskStatus status) {
        return status == null
                ? repository.findByOwnerId(ownerId)
                : repository.findByOwnerIdAndStatus(ownerId, status);
    }

    public Task get(Long ownerId, Long id) {
        return repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    // Replaces title/description/dueDate; a null status keeps the current one.
    public Task update(Long ownerId, Long id, TaskRequest request) {
        Task task = get(ownerId, id);
        apply(task, request);
        return repository.save(task);
    }

    public void delete(Long ownerId, Long id) {
        repository.delete(get(ownerId, id));
    }

    private void apply(Task task, TaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
    }
}
