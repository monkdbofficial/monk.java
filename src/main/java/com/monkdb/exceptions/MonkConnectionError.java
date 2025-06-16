package com.monkdb.exceptions;

import java.io.Serial;

public class MonkConnectionError extends MonkOperationalError {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkConnectionError(String message) {
        super(message);
    }

    public MonkConnectionError(String message, String errorTrace) {
        super(message, errorTrace);
    }

    public MonkConnectionError(String message, Throwable cause) {
        super(message, cause);
    }
}
