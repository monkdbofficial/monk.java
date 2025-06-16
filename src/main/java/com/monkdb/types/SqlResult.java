package com.monkdb.types;

import java.util.List;

public class SqlResult {
    private final List<String> cols;
    private final List<ColumnTypeDefinition> colTypes;
    private final List<List<Object>> rows;
    private final int rowCount;
    private final long duration;
    private final List<Result> results;

    public SqlResult(List<String> cols, List<ColumnTypeDefinition> colTypes, List<List<Object>> rows,
                     int rowCount, long duration, List<Result> results) {
        this.cols = cols;
        this.colTypes = colTypes;
        this.rows = rows;
        this.rowCount = rowCount;
        this.duration = duration;
        this.results = results;
    }

    public List<String> getCols() {
        return cols;
    }

    public List<ColumnTypeDefinition> getColTypes() {
        return colTypes;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rowCount;
    }

    public long getDuration() {
        return duration;
    }

    public List<Result> getResults() {
        return results;
    }

    // Nested class for result information
    public record Result(int rowCount, String errorMessage) {
    }
}

