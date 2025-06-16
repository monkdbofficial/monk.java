package com.monkdb.types;

/**
 * Represents either a base DataType or a nested array type like [ARRAY, INTEGER].
 */
public sealed interface ColumnTypeDefinition permits ColumnTypeDefinition.BaseType, ColumnTypeDefinition.ArrayType {

    record BaseType(DataType type) implements ColumnTypeDefinition {
    }

    record ArrayType(ColumnTypeDefinition elementType) implements ColumnTypeDefinition {
    }
}