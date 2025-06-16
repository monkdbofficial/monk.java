package com.monkdb.connection;

import com.monkdb.client.MonkClient;
import com.monkdb.client.MonkClientOptions;
import com.monkdb.exceptions.MonkProgrammingError;

import java.util.Objects;

public class MonkConnection {

    private final MonkClient client;
    private boolean closed = false;
    private final MonkConverter converter;

    public MonkConnection(MonkClientOptions options) {
        this(options, null, null);
    }

    public MonkConnection(MonkClientOptions options, MonkClient existingClient, MonkConverter overrideConverter) {
        Objects.requireNonNull(options, "MonkClientOptions cannot be null");

        if (options.getServers() == null || options.getServers().isEmpty()) {
            options.applyDefaults();
        }

        this.client = existingClient != null ? existingClient : new MonkClient(options);
        this.converter = overrideConverter != null ? overrideConverter : new MonkDefaultTypeConverter();
    }

    public MonkCursor cursor() throws MonkProgrammingError {
        if (closed) {
            throw new MonkProgrammingError("Connection is closed");
        }
        return new MonkCursor(this, converter);
    }

    public void close() {
        closed = true;
        client.close();
    }

    public boolean isClosed() {
        return closed;
    }

    public MonkClient getClient() {
        return client;
    }
}
