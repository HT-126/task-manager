package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository gives save/findById/findAll/deleteById for free; Spring generates the class.
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Derived query: Spring builds "WHERE status = ?" from the method name.
    List<Task> findByStatus(TaskStatus status);
}
