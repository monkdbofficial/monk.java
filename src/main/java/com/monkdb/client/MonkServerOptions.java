package com.monkdb.client;

public class MonkServerOptions {

    private final String username;
    private final String password;
    private final String schema;

    // Constructor
    public MonkServerOptions(String username, String password, String schema) {
        this.username = username;
        this.password = password;
        this.schema = schema;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSchema() {
        return schema;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String password;
        private String schema;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }

        public MonkServerOptions build() {
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Username is required");
            }
            return new MonkServerOptions(username, password, schema);
        }
    }
}
