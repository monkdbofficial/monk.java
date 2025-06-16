package com.monkdb.exceptions;

import java.io.Serial;

public class MonkErrors extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String errorTrace;

    public MonkErrors(String message) {
        this(message, null);
    }

    public MonkErrors(String message, String errorTrace) {
        super(message);
        this.errorTrace = errorTrace;
    }

    @Override
    public String toString() {
        return errorTrace == null ? super.toString() : super.toString() + "\n" + errorTrace;
    }
}

