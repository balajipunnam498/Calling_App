package com.prototype.calling.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ── Request DTOs ─────────────────────────────────────────────────────────────

class RegisterRequest {
    public String username;
    public String password;
    public String displayName;
}

class LoginRequest {
    public String username;
    public String password;
}

// ── Response DTOs ─────────────────────────────────────────────────────────────

@Data
@AllArgsConstructor
@NoArgsConstructor
class AuthResponse {
    private String token;
    private String username;
    private String displayName;
    private String message;
}