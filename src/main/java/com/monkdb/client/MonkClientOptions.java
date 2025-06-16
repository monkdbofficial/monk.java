package com.monkdb.client;

import java.util.List;
import java.util.Objects;

public class MonkClientOptions extends MonkServerOptions {

    private List<String> servers;
    private Integer retries;
    private Integer backoffFactor;
    private Integer retryInterval;
    private String path;
    private Boolean errorTrace;

    public MonkClientOptions(
            List<String> servers,
            Integer retries,
            Integer backoffFactor,
            Integer retryInterval,
            String path,
            Boolean errorTrace,
            String username,
            String password,
            String schema
    ) {
        super(username, password, schema);
        this.servers = servers;
        this.retries = retries;
        this.backoffFactor = backoffFactor;
        this.retryInterval = retryInterval;
        this.path = path;
        this.errorTrace = errorTrace;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getBackoffFactor() {
        return backoffFactor;
    }

    public void setBackoffFactor(Integer backoffFactor) {
        this.backoffFactor = backoffFactor;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getErrorTrace() {
        return errorTrace;
    }

    public void setErrorTrace(Boolean errorTrace) {
        this.errorTrace = errorTrace;
    }

    public void applyDefaults() {
        if (servers == null || servers.isEmpty()) {
            servers = List.of("http://127.0.0.1:4200");
        }
        retries = Objects.requireNonNullElse(retries, 3);
        backoffFactor = Objects.requireNonNullElse(backoffFactor, 100);
        retryInterval = Objects.requireNonNullElse(retryInterval, 30000);
        path = Objects.requireNonNullElse(path, "/_sql?types=true");
        errorTrace = Objects.requireNonNullElse(errorTrace, false);
    }
}
