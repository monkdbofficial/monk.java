package com.monkdb.exceptions;

import java.io.Serial;

public class MonkDatabaseError extends MonkErrors {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkDatabaseError(String message) {
        super(message);
    }

    public MonkDatabaseError(String message, String errorTrace) {
        super(message, errorTrace);
    }
}
