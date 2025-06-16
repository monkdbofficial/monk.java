package com.monkdb.types;

public enum DataType {
    NULL(0),
    NOT_SUPPORTED(1),
    CHAR(2),
    BOOLEAN(3),
    TEXT(4),
    IP(5),
    DOUBLE(6),
    REAL(7),
    SMALLINT(8),
    INTEGER(9),
    BIGINT(10),
    TIMESTAMP_WITH_TZ(11),
    OBJECT(12),
    GEOPOINT(13),
    GEOSHAPE(14),
    TIMESTAMP_WITHOUT_TZ(15),
    UNCHECKED_OBJECT(16),
    REGPROC(19),
    TIME(20),
    OIDVECTOR(21),
    NUMERIC(22),
    REGCLASS(23),
    DATE(24),
    BIT(25),
    JSON(26),
    CHARACTER(27),
    ARRAY(100);

    private final int code;

    DataType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DataType fromCode(int code) {
        for (DataType dataType : values()) {
            if (dataType.getCode() == code) {
                return dataType;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
