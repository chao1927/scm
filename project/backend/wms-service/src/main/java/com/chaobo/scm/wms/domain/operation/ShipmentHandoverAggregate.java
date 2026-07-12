package com.chaobo.scm.wms.domain.operation;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

public class ShipmentHandoverAggregate {
    private final long id;
    private final String handoverNo;
    private final long outboundId;
    private int status;
    private int version;

    public ShipmentHandoverAggregate(long id, String handoverNo, long outboundId, int status, int version) {
        if (handoverNo == null || handoverNo.isBlank() || outboundId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "交接单和出库单不能为空");
        }
        this.id = id;
        this.handoverNo = handoverNo;
        this.outboundId = outboundId;
        this.status = status;
        this.version = version;
    }

    public void confirm() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "发货交接单当前不可确认");
        }
        status = 2;
        version++;
    }

    public long id() { return id; }
    public String handoverNo() { return handoverNo; }
    public long outboundId() { return outboundId; }
    public int status() { return status; }
    public int version() { return version; }
}
