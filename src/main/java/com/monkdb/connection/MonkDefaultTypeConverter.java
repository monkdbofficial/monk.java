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
