package com.monkdb.types;

@FunctionalInterface
public interface ConverterFunction<Input, Output> {
    // Convert an input value to the desired output
    Output apply(Input value);

    // Convenience method to handle null values like TypeScript's null/undefined
    default Output applyWithNullCheck(Input value) {
        return (value == null) ? null : apply(value);
    }
}
