package com.monkdb.types;

public abstract class ColumnTypeDefinition {
    public static class SimpleColumnType extends ColumnTypeDefinition {
        private final DataType dataType;

        public SimpleColumnType(DataType dataType) {
            this.dataType = dataType;
        }

        public DataType getDataType() {
            return dataType;
        }
    }

    public static class ArrayColumnType extends ColumnTypeDefinition {
        private final ColumnTypeDefinition elementType;

        public ArrayColumnType(ColumnTypeDefinition elementType) {
            this.elementType = elementType;
        }

        public ColumnTypeDefinition getElementType() {
            return elementType;
        }
    }
}

