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
