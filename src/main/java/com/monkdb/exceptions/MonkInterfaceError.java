package com.monkdb.exceptions;

import java.io.Serial;

public class MonkInterfaceError extends MonkErrors {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkInterfaceError(String message) {
        super(message);
    }

    public MonkInterfaceError(String message, String errorTrace) {
        super(message, errorTrace);
    }
}
