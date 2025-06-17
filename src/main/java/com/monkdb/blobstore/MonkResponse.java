package com.monkdb.blobstore;

import java.io.InputStream;
import java.util.Map;

public record MonkResponse(int status, Map<String, Object> data, Map<String, String> headers, InputStream body) {
}
