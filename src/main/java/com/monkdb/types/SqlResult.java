// Copyright 2025, Movibase Platform Private Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.monkdb.types;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record SqlResult(
        List<String> cols,
        List<ColumnTypeDefinition> colTypes,
        List<List<Object>> rows,
        int rowcount,
        double duration,
        List<ResultInfo> results
) {
    public record ResultInfo(int rowcount, String errorMessage) {
    }

    @SuppressWarnings("unchecked")
    public static SqlResult from(Map<String, Object> map) {
        ObjectMapper mapper = new ObjectMapper();

        List<String> cols = (List<String>) map.getOrDefault("cols", List.of());
        List<Map<String, Object>> rawColTypes = (List<Map<String, Object>>) map.get("col_types");
        List<ColumnTypeDefinition> colTypes = new ArrayList<>();
        for (Map<String, Object> entry : rawColTypes) {
            String kind = (String) entry.get("kind");
            if ("base".equals(kind)) {
                int code = (int) entry.get("type");
                colTypes.add(new ColumnTypeDefinition.BaseType(DataType.fromCode(code)));
            } else if ("array".equals(kind)) {
                Map<String, Object> inner = (Map<String, Object>) entry.get("elementType");
                int code = (int) inner.get("type");
                colTypes.add(new ColumnTypeDefinition.ArrayType(
                        new ColumnTypeDefinition.BaseType(DataType.fromCode(code))));
            }
        }

        List<List<Object>> rows = (List<List<Object>>) map.getOrDefault("rows", List.of());
        int rowcount = (int) map.getOrDefault("rowcount", -1);
        double duration = ((Number) map.getOrDefault("duration", 0)).doubleValue();

        List<ResultInfo> results = null;
        if (map.containsKey("results")) {
            List<Map<String, Object>> rawResults = (List<Map<String, Object>>) map.get("results");
            results = new ArrayList<>();
            for (Map<String, Object> r : rawResults) {
                results.add(new ResultInfo((int) r.get("rowcount"), (String) r.get("error_message")));
            }
        }

        return new SqlResult(cols, colTypes, rows, rowcount, duration, results);
    }
}
