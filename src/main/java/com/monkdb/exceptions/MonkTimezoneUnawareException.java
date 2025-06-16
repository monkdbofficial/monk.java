package com.monkdb.exceptions;

import java.io.Serial;

public class MonkTimezoneUnawareException extends MonkErrors {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkTimezoneUnawareException(String message) {
        super(message);
    }

    public MonkTimezoneUnawareException(String message, String errorTrace) {
        super(message, errorTrace);
    }
}