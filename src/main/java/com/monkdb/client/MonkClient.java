// Copyright 2025, Movibase Platform Private Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.monkdb.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkdb.exceptions.MonkConnectionError;
import com.monkdb.exceptions.MonkProgrammingError;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class MonkClient {

    private static final Logger logger = Logger.getLogger(MonkClient.class.getName());

    private List<MonkServer> servers = new ArrayList<>();
    private Map<String, MonkServer> serverMap = new HashMap<>();
    private List<String> activeServers = new ArrayList<>();
    private List<TimestampedInactive> inactiveServers = new ArrayList<>();
    private int currentIndex = 0;
    private final int retries;
    private final int backoffFactor;
    private final int retry16Interval;
    public final String path;

    public MonkClient(MonkClientOptions options) {
        Objects.requireNonNull(options, "MonkClientOptions cannot be null");

        String[] serversArray = options.getServers() != null ? options.getServers().toArray(new String[0]) : new String[]{"http://127.0.0.1:4200"};
        this.retries = options.getRetries() != null ? options.getRetries() : 3;
        this.backoffFactor = options.getBackoffFactor() != null ? options.getBackoffFactor() : 100;
        this.retry16Interval = options.getRetryInterval() != null ? options.getRetryInterval() : 30000;
        this.path = options.getErrorTrace() ? options.getPath() + "&error_trace=true" : options.getPath();

        for (String url : serversArray) {
            MonkServer server = new MonkServer(url, options);
            this.servers.add(server);
            this.serverMap.put(url, server);
            this.activeServers.add(url);
        }
    }

    private MonkServer rotate() throws MonkConnectionError {
        restoreInactiveServers();
        if (activeServers.isEmpty()) {
            if (inactiveServers.isEmpty()) {
                throw new MonkConnectionError("No MonkDB servers available");
            } else {
                TimestampedInactive revived = inactiveServers.remove(0);
                activeServers.add(revived.url);
            }
        }
        String url = activeServers.get(currentIndex);
        currentIndex = (currentIndex + 1) % activeServers.size();
        return serverMap.get(url);
    }

    public CompletableFuture<Object> sql(String stmt, Object[] args, Object[][] bulkArgs) throws MonkProgrammingError {
        if (stmt == null || stmt.isEmpty()) {
            throw new MonkProgrammingError("SQL statement cannot be empty");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("stmt", stmt);
        if (bulkArgs != null) {
            body.put("bulk_args", bulkArgs);
        } else if (args != null) {
            body.put("args", args);
        }

        return requestWithRetries(() -> {
            try {
                MonkServer server = rotate();
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonBody = objectMapper.writeValueAsString(body);
                InputStream inputStream = new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8));

                return server.sendRequest("POST", path, inputStream, null)
                        .thenApply(response -> {
                            if (response.status() >= 500) {
                                throw new RuntimeException(new MonkConnectionError("MonkDB server error: " + response.status()));
                            }
                            JSONObject json = response.data();
                            return json != null ? json.toMap() : null;
                        });
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    public CompletableFuture<Map<String, Object>> request(String method, String path, Map<String, Object> body, Map<String, String> headers) {
        try {
            MonkServer server = rotate();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(body);
            InputStream inputStream = new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8));

            return server.sendRequest(method, path, inputStream, headers)
                    .thenApply(response -> {
                        JSONObject json = response.data();
                        return json != null ? json.toMap() : Collections.emptyMap();
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<Object> requestWith16Retries(Supplier<CompletableFuture<Object>> fnSupplier) {
        return CompletableFuture.supplyAsync(() -> {
            Exception lastError = null;
            for (int attempt = 0; attempt < retries; attempt++) {
                try {
                    Object response = fnSupplier.get().join();
                    if (response instanceof Map) {
                        Map<String, Object> responseMap = (Map<String, Object>) response;
                        int status = responseMap.containsKey("status") ? (int) responseMap.get("status") : 200;
                        if (status >= 500) {
                            lastError = new MonkConnectionError("MonkDB server responded with " + status + ": Retrying");
                            if (attempt < retries - 1) {
                                sleep(backoffFactor * Math.pow(2, attempt));
                                continue;
                            } else {
                                break;
                            }
                        }
                        return responseMap.get("data");
                    }
                    throw new MonkProgrammingError("Unexpected response: " + response);
                } catch (Exception e) {
                    lastError = e;
                    dropCurrentServer();
                    if (attempt < retries - 1) {
                        sleep(backoffFactor * Math.pow(2, attempt));
                    }
                }
            }
            throw new RuntimeException(
                    new MonkConnectionError("All servers failed. Last error: " +
                            (lastError != null ? lastError.getMessage() : "Unknown error"))
            );
        });
    }

    private CompletableFuture<Object> requestWithRetries(Supplier<CompletableFuture<Object>> fnSupplier) {
        return requestWith16Retries(fnSupplier);
    }

    private void dropCurrentServer() {
        if (!activeServers.isEmpty()) {
            String failed = activeServers.remove(currentIndex);
            inactiveServers.add(new TimestampedInactive(failed, System.currentTimeMillis()));
            if (currentIndex >= activeServers.size()) {
                currentIndex = 0;
            }
        }
    }

    private void restoreInactiveServers() {
        long now = System.currentTimeMillis();
        List<TimestampedInactive> stillInactive = new ArrayList<>();
        for (TimestampedInactive entry : inactiveServers) {
            if (now - entry.ts >= retry16Interval) {
                activeServers.add(entry.url);
            } else {
                stillInactive.add(entry);
            }
        }
        inactiveServers = stillInactive;
    }

    private void sleep(double ms) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void close() {
        servers.clear();
        serverMap.clear();
        activeServers.clear();
        inactiveServers.clear();
    }

    @Override
    public String toString() {
        return "<MonkClient " + String.join(", ", activeServers) + ">";
    }

    private record TimestampedInactive(String url, long ts) {
    }
}
