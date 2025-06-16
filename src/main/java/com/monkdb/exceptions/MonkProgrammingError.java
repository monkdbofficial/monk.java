package com.monkdb.exceptions;

import java.io.Serial;

public class MonkProgrammingError extends MonkDatabaseError {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkProgrammingError(String message) {
        super(message);
    }

    public MonkProgrammingError(String message, String errorTrace) {
        super(message, errorTrace);
    }
}
