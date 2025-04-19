package org.scm.tms._share.enums;

public enum LogisticsStatus {
    WAITING_PICKUP(1, "待揽收"),
    IN_TRANSIT(2, "运输中"),
    SIGNED(3, "已签收");

    private final int code;
    private final String description;

    LogisticsStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static LogisticsStatus of(Integer logisticStatus) {
        for (LogisticsStatus status : values()) {
            if (status.code == logisticStatus) {
                return status;
            }
        }
        return null;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
