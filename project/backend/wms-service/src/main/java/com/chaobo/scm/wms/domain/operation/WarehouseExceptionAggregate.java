package com.chaobo.scm.wms.domain.operation;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

public class WarehouseExceptionAggregate {
    private final long id;
    private final String exceptionNo;
    private final String reason;
    private int status;
    private int version;

    public WarehouseExceptionAggregate(long id, String exceptionNo, String reason, int status, int version) {
        if (exceptionNo == null || exceptionNo.isBlank() || reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "异常单号和原因不能为空");
        }
        this.id = id;
        this.exceptionNo = exceptionNo;
        this.reason = reason;
        this.status = status;
        this.version = version;
    }

    public void close() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "仓内异常当前不可关闭");
        }
        status = 2;
        version++;
    }

    public long id() { return id; }
    public String exceptionNo() { return exceptionNo; }
    public String reason() { return reason; }
    public int status() { return status; }
    public int version() { return version; }
}
