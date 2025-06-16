package com.monkdb.exceptions;

import java.io.Serial;

public class MonkInternalError extends MonkDatabaseError {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkInternalError(String message) {
        super(message);
    }

    public MonkInternalError(String message, String errorTrace) {
        super(message, errorTrace);
    }
}
