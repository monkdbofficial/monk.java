package com.monkdb.blobstore;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface MonkClientLike {
    CompletableFuture<MonkResponse> request(
            String method,
            String path,
            InputStream body,
            Map<String, String> headers
    );
}
