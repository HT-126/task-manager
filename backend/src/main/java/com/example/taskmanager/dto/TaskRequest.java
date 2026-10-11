package com.example.taskmanager.dto;

import com.example.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

// A record is an immutable data holder; Spring maps the JSON body onto it.
// @NotBlank: validation fails (HTTP 400) when title is missing or only spaces.
public record TaskRequest(
        @NotBlank String title,
        String description,
        TaskStatus status,
        LocalDate dueDate) {
}
