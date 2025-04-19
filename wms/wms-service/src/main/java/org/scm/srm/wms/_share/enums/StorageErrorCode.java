package org.scm.srm.wms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum StorageErrorCode implements ErrorCode {
    STORAGE_ALREADY_STARTED("S001", "入库已开始"),
    INVALID_INSPECT_STATUS("S002", "当前状态不能进行质检"),
    INVALID_SHELF_STATUS("S003", "当前状态不能进行上架");

    private final String code;
    private final String message;

    StorageErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
