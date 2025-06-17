package com.monkdb.client;

import com.monkdb.exceptions.MonkConnectionError;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class MonkServer {

    private static final Logger logger = Logger.getLogger(MonkServer.class.getName());

    private final String url;
    private final MonkServerOptions options;
    private final HttpClient client = HttpClient.newHttpClient();

    public MonkServer(String url, MonkServerOptions options) {
        this.url = Objects.requireNonNull(url, "URL cannot be null");
        this.options = Objects.requireNonNull(options, "MonkServerOptions cannot be null");
    }

    /**
     * Asynchronous method to send HTTP requests (GET, POST, etc.).
     *
     * @param method  HTTP method (GET, POST, etc.)
     * @param path    Path to the resource
     * @param body    Optional body of the request
     * @param headers Optional headers to include in the request
     * @return A CompletableFuture wrapping the Response
     */
    public CompletableFuture<Response> sendRequest(String method, String path, InputStream body, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> buildRequest(method, path, body, headers))
                .thenCompose(request -> client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()))
                .thenApply(response -> {
                    try {
                        InputStream responseStream = response.body(); // don't auto-close

                        JSONObject json = null;
                        String contentType = response.headers().firstValue("Content-Type").orElse("");

                        if (contentType.contains("application/json")) {
                            String responseData = new String(responseStream.readAllBytes());
                            json = responseData.isEmpty() ? null : new JSONObject(responseData);
                            // Re-wrap the stream since we've read it
                            responseStream = new ByteArrayInputStream(responseData.getBytes(StandardCharsets.UTF_8));
                            logger.info("Received JSON response: " + response.statusCode() + " " + responseData);
                        } else {
                            logger.info("Received non-JSON response: " + response.statusCode());
                        }

                        Map<String, String> responseHeaders = new HashMap<>();
                        response.headers().map().forEach((k, v) -> responseHeaders.put(k, String.join(",", v)));

                        return new Response(response.statusCode(), json, responseHeaders, responseStream);
                    } catch (Exception e) {
                        throw new RuntimeException("Error reading response", e);
                    }
                })
                .exceptionally(ex -> {
                    logger.severe("Failed request: " + ex.getMessage());
                    throw new RuntimeException(new MonkConnectionError("Failed to send request", ex));
                });
    }

    private HttpRequest buildRequest(String method, String path, InputStream body, Map<String, String> headers) {
        try {
            URI uri = new URL(new URL(this.url), path).toURI();
            HttpRequest.BodyPublisher bodyPublisher = (body != null)
                    ? HttpRequest.BodyPublishers.ofInputStream(() -> body)
                    : HttpRequest.BodyPublishers.noBody();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(uri)
                    .method(method, bodyPublisher)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json");

            if (options.getUsername() != null) {
                String credentials = options.getUsername() + ":" + (options.getPassword() != null ? options.getPassword() : "");
                String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
                builder.header("Authorization", "Basic " + encoded);
            }

            if (options.getSchema() != null) {
                builder.header("Default-Schema", options.getSchema());
            }

            if (headers != null) {
                headers.forEach(builder::header);
            }

            return builder.build();
        } catch (URISyntaxException e) {
            logger.severe("Invalid URI path: " + path);
            throw new RuntimeException(new MonkConnectionError("Invalid URI", e));
        } catch (Exception e) {
            throw new RuntimeException(new MonkConnectionError("Failed to build request", e));
        }
    }

    /**
     * Represents an HTTP response from the server.
     *
     * @param status  the HTTP status code
     * @param data    the parsed JSON data, if the response is JSON; otherwise null
     * @param headers the response headers
     * @param body    the response body as an InputStream; the caller is responsible for closing it
     */
    public record Response(int status, JSONObject data, Map<String, String> headers, InputStream body) {
    }
}
