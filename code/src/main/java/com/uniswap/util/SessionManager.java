package com.uniswap.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small in-memory "who is logged in" store.
 *
 * UniSwap is a single-user desktop app talking to a backend that only
 * ever listens on localhost, so a full JWT / Spring Security setup would
 * be overkill. Instead: on a successful login we hand the Swing client a
 * random token, and remember which user it belongs to. Every protected
 * request must send that token back in the "Authorization" header.
 *
 * Passwords themselves are still properly hashed with BCrypt (see
 * PasswordConfig) - this class only tracks *sessions*, not credentials.
 */
@Component
public class SessionManager {

    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

    public String createSession(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }

    public Long getUserId(String token) {
        if (token == null) return null;
        return tokenToUserId.get(token);
    }

    public void invalidate(String token) {
        if (token != null) {
            tokenToUserId.remove(token);
        }
    }
}
