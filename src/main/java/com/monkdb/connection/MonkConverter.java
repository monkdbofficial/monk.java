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

package com.monkdb.connection;

import com.monkdb.exceptions.MonkProgrammingError;
import com.monkdb.types.ColumnTypeDefinition;
import com.monkdb.types.ColumnTypeDefinition.ArrayType;
import com.monkdb.types.ColumnTypeDefinition.BaseType;
import com.monkdb.types.ConverterFunction;
import com.monkdb.types.DataType;

import java.util.*;

public class MonkConverter {

    protected final Map<DataType, ConverterFunction<?, ?>> mappings;
    protected final ConverterFunction<Object, Object> defaultConverter;

    public MonkConverter() {
        this(Collections.emptyMap(), value -> value);
    }

    public MonkConverter(Map<DataType, ConverterFunction<?, ?>> initial, ConverterFunction<Object, Object> fallback) {
        this.mappings = new EnumMap<>(DataType.class);
        this.mappings.putAll(initial);
        this.defaultConverter = fallback;
    }

    @SuppressWarnings("unchecked")
    public ConverterFunction<Object, Object> get(ColumnTypeDefinition type) throws MonkProgrammingError {
        if (type instanceof BaseType base) {
            ConverterFunction<?, ?> fn = mappings.getOrDefault(base.type(), defaultConverter);
            return (ConverterFunction<Object, Object>) fn;
        }

        if (type instanceof ArrayType arrayType) {
            ConverterFunction<Object, Object> inner = get(arrayType.elementType());
            return value -> {
                if (!(value instanceof List<?> list)) return null;
                List<Object> result = new ArrayList<>(list.size());
                for (Object v : list) {
                    result.add(inner.apply(v));
                }
                return result;
            };
        }

        throw new MonkProgrammingError("Unsupported ColumnTypeDefinition structure: " + type);
    }

    public void set(DataType type, ConverterFunction<?, ?> converter) {
        mappings.put(type, converter);
    }
}
