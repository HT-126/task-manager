package com.example.taskmanager.auth;

import com.example.taskmanager.exception.InvalidCredentialsException;
import com.example.taskmanager.exception.UsernameTakenException;
import com.example.taskmanager.model.RevokedToken;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.RevokedTokenRepository;
import com.example.taskmanager.repository.UserRepository;
import java.time.Instant;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final RevokedTokenRepository revoked;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    // Hash of a throwaway password: checked when the user does not exist, so a missing
    // user takes about as long as a wrong password (no user-enumeration by timing).
    private final String dummyHash;

    public AuthService(UserRepository users, RevokedTokenRepository revoked,
                       PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.revoked = revoked;
        this.encoder = encoder;
        this.jwt = jwt;
        this.dummyHash = encoder.encode("not-a-real-password");
    }

    public User register(RegisterRequest request) {
        String username = normalize(request.username());
        if (users.existsByUsername(username)) {
            throw new UsernameTakenException();
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(request.password()));
        try {
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Two requests registered the same name at once; the unique constraint caught it.
            throw new UsernameTakenException();
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = users.findByUsername(normalize(request.username())).orElse(null);
        boolean ok = encoder.matches(request.password(),
                user != null ? user.getPasswordHash() : dummyHash);
        if (user == null || !ok) {
            throw new InvalidCredentialsException();
        }
        JwtService.Issued issued = jwt.issue(user);
        return new AuthResponse(issued.token(), user.getUsername(), issued.expiresAt());
    }

    public void logout(Jwt token) {
        Instant now = Instant.now();
        revoked.deleteByExpiresAtBefore(now);
        if (!revoked.existsById(token.getId())) {
            revoked.save(new RevokedToken(token.getId(), token.getExpiresAt()));
        }
    }

    private static String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
