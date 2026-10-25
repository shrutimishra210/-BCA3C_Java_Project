package com.uniswap.util;

import com.uniswap.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Small helper controllers use to resolve "who is calling this endpoint"
 * from the Authorization header, and to reject the call with a clean
 * 401 error if the token is missing or expired (e.g. after a restart).
 *
 * Expected header format:  Authorization: Bearer <token>
 */
@Component
public class AuthHelper {

    private final SessionManager sessionManager;

    @Autowired
    public AuthHelper(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public Long requireUserId(String authHeader) {
        String token = extractToken(authHeader);
        Long userId = sessionManager.getUserId(token);
        if (userId == null) {
            throw ApiException.unauthorized("Your session has expired. Please log in again.");
        }
        return userId;
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length()).trim();
        }
        return authHeader;
    }
}
