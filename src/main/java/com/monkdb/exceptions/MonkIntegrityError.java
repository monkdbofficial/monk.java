package com.monkdb.exceptions;

import java.io.Serial;

public class MonkIntegrityError extends MonkDatabaseError {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkIntegrityError(String message) {
        super(message);
    }

    public MonkIntegrityError(String message, String errorTrace) {
        super(message, errorTrace);
    }
}
