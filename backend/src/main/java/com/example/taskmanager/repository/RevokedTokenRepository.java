package com.example.taskmanager.repository;

import com.example.taskmanager.model.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    // Derived delete: removes tokens that have expired anyway.
    void deleteByExpiresAtBefore(Instant time);
}
