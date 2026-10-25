package com.uniswap.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Everything the Swing GUI knows about talking to the backend lives here.
 * No screen ever opens a socket or builds JSON itself - it just calls
 * ApiClient.get/post/put/delete and gets back plain Java objects (or a
 * readable ApiClientException if the backend rejected the request).
 *
 * This is the one class that embodies "Swing GUI -> Spring Boot backend"
 * from the architecture diagram: every other GUI class only ever talks to
 * this class, never to HttpClient directly.
 */
public final class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Set once, right after a successful login/sign-up. */
    private static volatile String authToken;

    private ApiClient() {
    }

    /** Downloads raw bytes (used for product photos). Returns null on any failure. */
    static byte[] fetchBytes(String url) {
        try {
            HttpResponse<byte[]> r = HTTP.send(HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            return r.statusCode() == 200 ? r.body() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static void clearAuthToken() {
        authToken = null;
    }

    public static <T> T get(String path, Class<T> responseType) {
        return send(request(path, "GET", null), responseType);
    }

    public static <T> List<T> getList(String path, TypeReference<List<T>> typeRef) {
        return sendList(request(path, "GET", null), typeRef);
    }

    public static <T> T post(String path, Object body, Class<T> responseType) {
        return send(request(path, "POST", body), responseType);
    }

    public static void post(String path, Object body) {
        send(request(path, "POST", body), Void.class);
    }

    public static <T> T put(String path, Object body, Class<T> responseType) {
        return send(request(path, "PUT", body), responseType);
    }

    public static void put(String path) {
        send(request(path, "PUT", null), Void.class);
    }

    public static void delete(String path) {
        send(request(path, "DELETE", null), Void.class);
    }

    /** Absolute URL for a server-relative path such as /uploads/listings/x.jpg. */
    public static String resolveUrl(String relativeUrl) {
        if (relativeUrl == null) return null;
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) return relativeUrl;
        return BASE_URL.substring(0, BASE_URL.length() - "/api".length()) + relativeUrl;
    }

    /**
     * Uploads a product photo (multipart/form-data) and returns the server-relative image URL.
     * The Content-Type is multipart with a generated boundary - NOT application/json.
     */
    public static String uploadImage(java.io.File file) {
        try {
            String boundary = "----SwapUni" + java.util.UUID.randomUUID().toString().replace("-", "");
            String name = file.getName().replace("\"", "_").replace("\r", "_").replace("\n", "_");
            String lower = name.toLowerCase();
            String mime = lower.endsWith(".png") ? "image/png" : lower.endsWith(".gif") ? "image/gif"
                    : lower.endsWith(".webp") ? "image/webp" : "image/jpeg";
            byte[] data = java.nio.file.Files.readAllBytes(file.toPath());

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(data.length + 512);
            out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + name
                    + "\"\r\nContent-Type: " + mime + "\r\n\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.write(data);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/listings/upload-image"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()));
            if (authToken != null) {
                builder.header("Authorization", "Bearer " + authToken);
            }
            Map<?, ?> result = send(builder.build(), Map.class);
            Object url = result == null ? null : result.get("imageUrl");
            if (url == null) {
                throw new ApiClientException("The server didn't return an image URL.");
            }
            return url.toString();
        } catch (IOException e) {
            throw new ApiClientException("Couldn't read the image file: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------

    private static HttpRequest request(String path, String method, Object body) {
        try {
            String json = body == null ? "" : MAPPER.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .header("Content-Type", "application/json");

            if (authToken != null) {
                builder.header("Authorization", "Bearer " + authToken);
            }

            HttpRequest.BodyPublisher publisher = body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(json);

            builder.method(method, publisher);
            return builder.build();
        } catch (IOException e) {
            throw new ApiClientException("Could not prepare the request: " + e.getMessage());
        }
    }

    private static <T> T send(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            checkStatus(response);
            if (responseType == Void.class || response.body() == null || response.body().isBlank()) {
                return null;
            }
            return MAPPER.readValue(response.body(), responseType);
        } catch (ApiClientException e) {
            throw e;
        } catch (java.net.ConnectException e) {
            throw new ApiClientException("Can't reach the SwapUni backend. Is it still running?");
        } catch (Exception e) {
            throw new ApiClientException("Unexpected error: " + e.getMessage());
        }
    }

    private static <T> List<T> sendList(HttpRequest request, TypeReference<List<T>> typeRef) {
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            checkStatus(response);
            if (response.body() == null || response.body().isBlank()) {
                return List.of();
            }
            return MAPPER.readValue(response.body(), typeRef);
        } catch (ApiClientException e) {
            throw e;
        } catch (java.net.ConnectException e) {
            throw new ApiClientException("Can't reach the SwapUni backend. Is it still running?");
        } catch (Exception e) {
            throw new ApiClientException("Unexpected error: " + e.getMessage());
        }
    }

    private static void checkStatus(HttpResponse<String> response) throws IOException {
        if (response.statusCode() >= 400) {
            String message = "Something went wrong (HTTP " + response.statusCode() + ").";
            try {
                Map<?, ?> body = MAPPER.readValue(response.body(), Map.class);
                if (body.get("message") != null) {
                    message = body.get("message").toString();
                }
            } catch (Exception ignored) {
                // Body wasn't JSON - fall back to the generic message above.
            }
            throw new ApiClientException(message);
        }
    }
}
