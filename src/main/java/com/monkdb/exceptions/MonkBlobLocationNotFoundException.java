package com.monkdb.exceptions;

import java.io.Serial;

public class MonkBlobLocationNotFoundException extends MonkBlobException {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkBlobLocationNotFoundException(String table, String digest) {
        super(table, digest);
    }
}
