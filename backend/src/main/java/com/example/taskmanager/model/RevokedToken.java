package com.example.taskmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

// A logged-out token. jti is the token's unique id; rows can be purged after expiresAt
// because an expired token is rejected anyway.
@Entity
public class RevokedToken {

    @Id
    private String jti;

    private Instant expiresAt;

    protected RevokedToken() {
    }

    public RevokedToken(String jti, Instant expiresAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
    }

    public String getJti() { return jti; }
    public Instant getExpiresAt() { return expiresAt; }
}
