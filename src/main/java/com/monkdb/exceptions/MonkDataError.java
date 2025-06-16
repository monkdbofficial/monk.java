package com.monkdb.exceptions;

import java.io.Serial;

public class MonkDataError extends MonkDatabaseError {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkDataError(String message) {
        super(message);
    }

    public MonkDataError(String message, String errorTrace) {
        super(message, errorTrace);
    }
}
