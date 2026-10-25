package com.uniswap.gui;

/** Thrown by ApiClient when the backend returns an error response body. */
public class ApiClientException extends RuntimeException {
    public ApiClientException(String message) {
        super(message);
    }
}
