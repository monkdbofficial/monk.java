package com.monkdb.types;

@FunctionalInterface
public interface ConverterFunction<Input, Output> {
    Output apply(Input value);

    static <T> T safeApply(ConverterFunction<T, T> fn, T value) {
        return value == null ? null : fn.apply(value);
    }
}