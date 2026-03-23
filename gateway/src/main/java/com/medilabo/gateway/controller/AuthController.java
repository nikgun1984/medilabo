package com.medilabo.gateway.controller;

import com.medilabo.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Authentication controller. Provides a login endpoint that returns a JWT.
 *
 * In a production system this would validate credentials against a user database.
 * For this project, we use a simple hardcoded credential check.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    // Hardcoded users for demo purposes — in production, use a DB / LDAP / OAuth2 provider
    private static final Map<String, String> USERS = Map.of(
            "doctor", "doctor123",
            "admin", "admin123"
    );

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        log.info("Login attempt for user='{}'", username);

        if (username == null || password == null) {
            log.warn("Login failed — missing username or password");
            return Mono.just(ResponseEntity.badRequest().body(
                    Map.of("error", "Username and password are required")
            ));
        }

        String expectedPassword = USERS.get(username);
        if (expectedPassword != null && expectedPassword.equals(password)) {
            String token = jwtUtil.generateToken(username);
            log.info("Login successful for user='{}'", username);
            return Mono.just(ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", username,
                    "message", "Login successful"
            )));
        }

        log.warn("Login failed for user='{}' — invalid credentials", username);
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("error", "Invalid username or password")
        ));
    }
}
