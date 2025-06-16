package com.monkdb.exceptions;

import java.io.Serial;

public class MonkErrors extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String errorTrace;

    /**
     * Constructs a new exception with the specified detail message.
     * The error trace is set to null.
     *
     * @param message the detail message
     */
    public MonkErrors(String message) {
        this(message, (String) null);
    }

    /**
     * Constructs a new exception with the specified detail message and error trace.
     *
     * @param message the detail message
     * @param errorTrace additional error trace information
     */
    public MonkErrors(String message, String errorTrace) {
        super(message);
        this.errorTrace = errorTrace;
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * The error trace is set to null.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by getCause())
     */
    public MonkErrors(String message, Throwable cause) {
        super(message, cause);
        this.errorTrace = null;
    }

    @Override
    public String toString() {
        return errorTrace == null ? super.toString() : super.toString() + "\n" + errorTrace;
    }
}

