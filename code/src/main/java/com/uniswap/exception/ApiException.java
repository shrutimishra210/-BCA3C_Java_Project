package com.uniswap.exception;

import org.springframework.http.HttpStatus;

/**
 * A predictable, expected error (bad input, not found, unauthorized, etc.)
 * that should be shown to the user as a readable message rather than a
 * stack trace. Thrown from the service layer, turned into a clean JSON
 * error body by GlobalExceptionHandler, and shown in the Swing GUI via
 * JOptionPane.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static ApiException badRequest(String message) {
        return new ApiException(message, HttpStatus.BAD_REQUEST);
    }

    public static ApiException notFound(String message) {
        return new ApiException(message, HttpStatus.NOT_FOUND);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(message, HttpStatus.UNAUTHORIZED);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(message, HttpStatus.FORBIDDEN);
    }

    public static ApiException conflict(String message) {
        return new ApiException(message, HttpStatus.CONFLICT);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
