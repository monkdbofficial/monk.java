package com.monkdb.client;

public final class MonkServerOptions {
    private final String username;
    private final String password;
    private final String schema;

    // Constructor initializes the options
    private MonkServerOptions(String username, String password, String schema) {
        this.username = username;
        this.password = password;
        this.schema = schema;
    }

    // Getters for accessing the properties
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSchema() {
        return schema;
    }

    // Builder pattern for constructing options in an immutable way
    public static class Builder {
        private String username;
        private String password;
        private String schema;

        // Setters with validation
        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setSchema(String schema) {
            this.schema = schema;
            return this;
        }

        // Validate the options
        private void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be null or empty");
            }
            // Password and schema are optional, so no validation needed
        }

        // Build the MonkServerOptions object
        public MonkServerOptions build() {
            validate();
            return new MonkServerOptions(username, password, schema);
        }
    }
}