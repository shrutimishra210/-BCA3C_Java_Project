package com.uniswap.controller;

import com.uniswap.dto.*;
import com.uniswap.service.AuthService;
import com.uniswap.util.AuthHelper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthHelper authHelper;

    public AuthController(AuthService authService, AuthHelper authHelper) {
        this.authService = authService;
        this.authHelper = authHelper;
    }

    @PostMapping("/signup")
    public AuthResponse signUp(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader == null ? null : authHeader.replace("Bearer ", "").trim());
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = authHelper.requireUserId(authHeader);
        return authService.getProfile(userId);
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody ProfileUpdateRequest request) {
        Long userId = authHelper.requireUserId(authHeader);
        return authService.updateProfile(userId, request);
    }
}
