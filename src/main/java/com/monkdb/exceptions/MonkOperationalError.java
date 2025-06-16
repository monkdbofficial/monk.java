package com.monkdb.exceptions;

import java.io.Serial;

public class MonkOperationalError extends MonkDatabaseError {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkOperationalError(String message) {
        super(message);
    }

    public MonkOperationalError(String message, String errorTrace) {
        super(message, errorTrace);
    }
}
