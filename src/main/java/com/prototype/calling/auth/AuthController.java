package com.prototype.calling.auth;

import com.prototype.calling.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Handles user registration and login.
 *
 * POST /api/auth/register  → create new user
 * POST /api/auth/login     → verify credentials → return JWT
 * GET  /api/auth/me        → return current user info (requires token)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ── REGISTER ─────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        // Check username not already taken
        if (userRepository.existsByUsername(request.username)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, null, null,
                            "Username '" + request.username + "' is already taken"));
        }

        // Save user with hashed password — NEVER store plain text
        User user = User.builder()
                .username(request.username.toLowerCase().trim())
                .password(passwordEncoder.encode(request.password))
                .displayName(request.displayName != null ? request.displayName : request.username)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        // Generate JWT and return it so user is logged in immediately after registering
        String token = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getUsername(), user.getDisplayName(),
                        "Registration successful!"));
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        // Find user by username
        User user = userRepository.findByUsername(request.username.toLowerCase().trim())
                .orElse(null);

        // Check user exists AND password matches
        if (user == null || !passwordEncoder.matches(request.password, user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null,
                            "Invalid username or password"));
        }

        log.info("User logged in: {}", user.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.ok(
                new AuthResponse(token, user.getUsername(), user.getDisplayName(),
                        "Login successful!"));
    }

    // ── GET CURRENT USER ──────────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(
                new AuthResponse(null, user.getUsername(), user.getDisplayName(), null));
    }
}