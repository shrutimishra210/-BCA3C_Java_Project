package com.uniswap.service;

import com.uniswap.dto.*;
import com.uniswap.exception.ApiException;
import com.uniswap.model.User;
import com.uniswap.repository.UserRepository;
import com.uniswap.util.SessionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionManager sessionManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionManager = sessionManager;
    }

    public AuthResponse signUp(SignUpRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw ApiException.conflict("An account with this email already exists.");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw ApiException.conflict("This username is already taken.");
        }

        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setCourse(req.getCourse());
        user.setYear(req.getYear());
        user.setCampus(req.getCampus());
        userRepository.save(user);

        String token = sessionManager.createSession(user.getId());
        return new AuthResponse(token, user.getId(), user.getFullName());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmailOrUsername())
                .or(() -> userRepository.findByUsername(req.getEmailOrUsername()))
                .orElseThrow(() -> ApiException.badRequest("No account found with that email/username."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Incorrect password. Please try again.");
        }

        String token = sessionManager.createSession(user.getId());
        return new AuthResponse(token, user.getId(), user.getFullName());
    }

    public void logout(String token) {
        sessionManager.invalidate(token);
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Your session is invalid. Please log in again."));
    }

    public ProfileResponse getProfile(Long userId) {
        User u = requireUser(userId);
        ProfileResponse p = new ProfileResponse();
        p.setId(u.getId());
        p.setFullName(u.getFullName());
        p.setEmail(u.getEmail());
        p.setUsername(u.getUsername());
        p.setCourse(u.getCourse());
        p.setYear(u.getYear());
        p.setCampus(u.getCampus());
        p.setBio(u.getBio());
        return p;
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest req) {
        User u = requireUser(userId);
        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            u.setFullName(req.getFullName());
        }
        u.setCourse(req.getCourse());
        u.setYear(req.getYear());
        u.setCampus(req.getCampus());
        u.setBio(req.getBio());
        userRepository.save(u);
        return getProfile(userId);
    }
}
