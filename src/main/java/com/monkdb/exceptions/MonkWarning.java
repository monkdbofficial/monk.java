package com.monkdb.exceptions;

import java.io.Serial;

public class MonkWarning extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkWarning(String message) {
        super(message);
    }
}