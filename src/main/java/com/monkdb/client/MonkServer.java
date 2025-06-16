package com.monkdb.client;

import com.monkdb.exceptions.MonkConnectionError;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class MonkServer {

    private static final Logger logger = Logger.getLogger(MonkServer.class.getName());
    private final String url;
    private final MonkServerOptions options;

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
     * @return A CompletableFuture wrapping the response
     * @throws MonkConnectionError If there is a failure in sending the request
     */
    public CompletableFuture<Response> sendRequestAsync(String method, String path, InputStream body, Map<String, String> headers) throws MonkConnectionError {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL fullUrl = new URL(new URL(this.url), path);
                URI uri;
                try {
                    uri = fullUrl.toURI();
                } catch (URISyntaxException e) {
                    logger.severe("Invalid URI: " + fullUrl.toString());
                    throw new MonkConnectionError("Failed to create URI from URL: " + fullUrl.toString(), e);
                }

                HttpClient client = HttpClient.newHttpClient();

                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(uri)
                        .method(method, body != null ? HttpRequest.BodyPublishers.ofInputStream(() -> body) : HttpRequest.BodyPublishers.noBody())
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json");

                if (this.options.getUsername() != null) {
                    String credentials = this.options.getUsername() + ":" + (this.options.getPassword() != null ? this.options.getPassword() : "");
                    String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                    requestBuilder.header("Authorization", "Basic " + encodedCredentials);
                }

                if (this.options.getSchema() != null) {
                    requestBuilder.header("Default-Schema", this.options.getSchema());
                }

                if (headers != null) {
                    headers.forEach(requestBuilder::header);
                }

                HttpRequest request = requestBuilder.build();

                HttpResponse<InputStream> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).get();

                InputStream responseStream = response.body();
                String responseData = new String(responseStream.readAllBytes());

                JSONObject jsonResponse = responseData.isEmpty() ? null : new JSONObject(responseData);

                logger.info("Received response: " + response.statusCode() + " " + responseData);

                Map<String, String> responseHeaders = new HashMap<>();
                response.headers().map().forEach((key, value) -> responseHeaders.put(key, String.join(",", value)));

                return new Response(response.statusCode(), jsonResponse, responseHeaders);
            } catch (IOException | InterruptedException | ExecutionException | MonkConnectionError e) {
                logger.severe("Error sending request: " + e.getMessage());
                try {
                    throw new MonkConnectionError("Failed to send request", e);
                } catch (MonkConnectionError ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    // Response object to wrap HTTP response details
    public record Response(int status, JSONObject data, Map<String, String> headers) {
    }
}
