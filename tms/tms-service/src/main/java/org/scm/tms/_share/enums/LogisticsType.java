package org.scm.tms._share.enums;

public enum LogisticsType {
    NEXT_DAY(1, "次日达"),
    COLD_CHAIN(2, "冷链运输");

    private final int code;
    private final String description;

    LogisticsType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
