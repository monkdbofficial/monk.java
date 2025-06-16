package com.monkdb.exceptions;

import java.io.Serial;

public class MonkBlobException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String table;
    private final String digest;

    public MonkBlobException(String table, String digest) {
        super(table + "/" + digest);
        this.table = table;
        this.digest = digest;
    }

    public String getTable() {
        return table;
    }

    public String getDigest() {
        return digest;
    }
}
