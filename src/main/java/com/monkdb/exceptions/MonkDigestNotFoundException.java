package com.monkdb.exceptions;

import java.io.Serial;

public class MonkDigestNotFoundException extends MonkBlobException {
    @Serial
    private static final long serialVersionUID = 1L;

    public MonkDigestNotFoundException(String table, String digest) {
        super(table, digest);
    }
}
