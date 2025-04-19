package org.scm.srm.wms._share.enums;

public enum SortingOrderStatus {

    SORTING(1, "拣货中"),
    COMPLETED(2, "拣货完成");

    private final int code;
    private final String description;

    SortingOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
