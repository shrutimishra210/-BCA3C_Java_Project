package com.uniswap.dto;

public class AuthResponse {
    private String token;
    private Long userId;
    private String fullName;

    // No-arg constructor required so Jackson can deserialize this on the
    // Swing side (the client parses the JSON response from /auth/login and
    // /auth/signup back into this class - without this constructor Jackson
    // has no way to construct an empty instance to then fill in via setters).
    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String fullName) {
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
