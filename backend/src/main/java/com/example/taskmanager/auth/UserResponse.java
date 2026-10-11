package com.example.taskmanager.auth;

import com.example.taskmanager.model.User;

public record UserResponse(Long id, String username) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername());
    }
}
