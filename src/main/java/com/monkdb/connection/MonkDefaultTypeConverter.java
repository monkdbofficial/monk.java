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

import com.monkdb.types.ConverterFunction;
import com.monkdb.types.DataType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.Map;

public class MonkDefaultTypeConverter extends MonkConverter {

    public MonkDefaultTypeConverter() {
        this(null);
    }

    public MonkDefaultTypeConverter(Map<DataType, ConverterFunction<?, ?>> overrides) {
        super(buildDefaultMappings(overrides), value -> value);
    }

    private static Map<DataType, ConverterFunction<?, ?>> buildDefaultMappings(Map<DataType, ConverterFunction<?, ?>> overrides) {
        Map<DataType, ConverterFunction<?, ?>> map = new EnumMap<>(DataType.class);

        // IP address validation
        map.put(DataType.IP, value -> {
            if (value == null) return null;
            try {
                return InetAddress.getByName(value.toString()).getHostAddress();
            } catch (UnknownHostException e) {
                return null;
            }
        });

        // Timestamp conversion
        ConverterFunction<Number, java.util.Date> timestampFn = millis ->
                millis == null ? null : new java.util.Date(millis.longValue());

        map.put(DataType.TIMESTAMP_WITH_TZ, timestampFn);
        map.put(DataType.TIMESTAMP_WITHOUT_TZ, timestampFn);

        // Apply overrides
        if (overrides != null) {
            map.putAll(overrides);
        }

        return map;
    }
}
