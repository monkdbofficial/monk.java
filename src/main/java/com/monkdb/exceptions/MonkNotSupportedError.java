package com.monkdb.exceptions;

import java.io.Serial;

public class MonkNotSupportedError extends MonkDatabaseError {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkNotSupportedError(String message) {
        super(message);
    }

    public MonkNotSupportedError(String message, String errorTrace) {
        super(message, errorTrace);
    }
}
