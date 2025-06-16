package com.monkdb.connection;

import com.monkdb.exceptions.MonkProgrammingError;
import com.monkdb.types.ColumnTypeDefinition;
import com.monkdb.types.ConverterFunction;
import com.monkdb.types.SqlResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class MonkCursor {

    private final MonkConnection connection;
    private final MonkConverter converter;

    private SqlResult result = null;
    private int index = -1;
    private List<List<Object>> rows = new ArrayList<>();
    private Map<String, Integer> colIndex = new HashMap<>();
    private List<ConverterFunction<Object, Object>> converters = new ArrayList<>();

    public MonkCursor(MonkConnection connection, MonkConverter converter) {
        this.connection = connection;
        this.converter = converter;
    }

    public CompletableFuture<Void> execute(String sql, Object[] params) throws MonkProgrammingError {
        if (connection.isClosed()) throw new MonkProgrammingError("Connection has been closed");

        return connection.getClient().sql(sql, params, null)
                .thenApply(raw -> {
                    SqlResult resultObj = SqlResult.from((Map<String, Object>) raw);
                    this.result = resultObj;
                    this.rows = convertRows(resultObj);
                    this.index = -1;

                    colIndex.clear();
                    List<String> cols = resultObj.cols();
                    for (int i = 0; i < cols.size(); i++) {
                        colIndex.put(cols.get(i), i);
                    }
                    return null;
                });
    }

    public boolean next() {
        if (result == null) return false;
        index++;
        return index < rows.size();
    }

    public Object getObject(int columnIndex) throws MonkProgrammingError {
        return getCurrentRow().get(columnIndex);
    }

    public Object getObject(String columnName) throws MonkProgrammingError {
        Integer i = colIndex.get(columnName);
        if (i == null) throw new MonkProgrammingError("Column not found: " + columnName);
        return getCurrentRow().get(i);
    }

    public String getString(String columnName) throws MonkProgrammingError {
        Object val = getObject(columnName);
        return val != null ? val.toString() : null;
    }

    public int getInt(String columnName) throws MonkProgrammingError {
        Object val = getObject(columnName);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    public List<Object> fetchone() throws MonkProgrammingError {
        return next() ? getCurrentRow() : null;
    }

    public List<List<Object>> fetchmany(int count) throws MonkProgrammingError {
        List<List<Object>> fetched = new ArrayList<>();
        while (count-- > 0 && next()) {
            fetched.add(getCurrentRow());
        }
        return fetched;
    }

    public List<List<Object>> fetchall() throws MonkProgrammingError {
        List<List<Object>> all = new ArrayList<>();
        while (next()) all.add(getCurrentRow());
        return all;
    }

    public Stream<List<Object>> stream() {
        return rows.subList(index + 1, rows.size()).stream();
    }

    public void close() {
        result = null;
        rows.clear();
        converters.clear();
        index = -1;
    }

    public int getRowcount() {
        return result != null ? result.rowcount() : -1;
    }

    public List<List<Object>> getDescription() {
        if (result == null) return null;
        List<List<Object>> desc = new ArrayList<>();
        for (String col : result.cols()) {
            List<Object> meta = new ArrayList<>(7);
            meta.add(col);
            for (int i = 0; i < 6; i++) meta.add(null);
            desc.add(meta);
        }
        return desc;
    }

    private List<List<Object>> convertRows(SqlResult result) {
        converters = result.colTypes().stream()
                .map(this::safeGet)
                .toList();

        List<List<Object>> converted = new ArrayList<>();
        for (List<Object> row : result.rows()) {
            List<Object> newRow = new ArrayList<>(row.size());
            for (int i = 0; i < row.size(); i++) {
                newRow.add(converters.get(i).apply(row.get(i)));
            }
            converted.add(newRow);
        }
        return converted;
    }

    private ConverterFunction<Object, Object> safeGet(ColumnTypeDefinition type) {
        try {
            return converter.get(type);
        } catch (MonkProgrammingError e) {
            throw new RuntimeException("Converter failed for column type: " + type, e);
        }
    }


    private List<Object> getCurrentRow() throws MonkProgrammingError {
        if (index < 0 || index >= rows.size()) throw new MonkProgrammingError("Cursor is not at a valid row");
        return rows.get(index);
    }
}
