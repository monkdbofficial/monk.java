package com.monkdb.types;

import java.util.List;

public record SqlResult(
        List<String> cols,
        List<ColumnTypeDefinition> colTypes,
        List<List<Object>> rows,
        int rowcount,
        double duration,
        List<ResultInfo> results
) {
    public record ResultInfo(
            int rowcount,
            String errorMessage
    ) {
    }
}
