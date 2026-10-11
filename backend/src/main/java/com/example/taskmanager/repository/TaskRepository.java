package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository gives save/findById/findAll/deleteById for free; Spring generates the class.
// Every finder takes the owner id, so one user can never read or change another user's task.
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Derived query: Spring builds "WHERE owner_id = ?" from the method name.
    List<Task> findByOwnerId(Long ownerId);

    List<Task> findByOwnerIdAndStatus(Long ownerId, TaskStatus status);

    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
}
