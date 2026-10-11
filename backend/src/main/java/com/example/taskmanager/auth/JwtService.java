package com.example.taskmanager.auth;

import com.example.taskmanager.model.User;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

// Builds signed JWTs. A JWT is a signed JSON token: the server can verify it later without
// looking anything up, which is why the API needs no session.
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final Duration lifetime;

    public JwtService(JwtEncoder encoder,
                      @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.encoder = encoder;
        this.lifetime = Duration.ofMinutes(expirationMinutes);
    }

    public record Issued(String token, Instant expiresAt) {
    }

    public Issued issue(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(lifetime);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(user.getId()))   // "sub": who the token is for
                .id(UUID.randomUUID().toString())        // "jti": unique id, used for logout
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Issued(token, expiresAt);
    }
}
