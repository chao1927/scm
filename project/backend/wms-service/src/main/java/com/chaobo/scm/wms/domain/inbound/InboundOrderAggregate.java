package com.chaobo.scm.wms.domain.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.time.OffsetDateTime;

public class InboundOrderAggregate {
    private final long id;
    private final String inboundNo;
    private final String sourceType;
    private final String sourceNo;
    private final long warehouseId;
    private InboundOrderStatus status;
    private OffsetDateTime expectedArrivalAt;
    private String cancelReason;
    private int version;

    public InboundOrderAggregate(long id, String inboundNo, String sourceType, String sourceNo, long warehouseId,
                                 InboundOrderStatus status, OffsetDateTime expectedArrivalAt,
                                 String cancelReason, int version) {
        if (sourceType == null || sourceType.isBlank() || sourceNo == null || sourceNo.isBlank() || warehouseId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入库来源、来源单号和仓库不能为空");
        }
        this.id = id;
        this.inboundNo = inboundNo;
        this.sourceType = sourceType;
        this.sourceNo = sourceNo;
        this.warehouseId = warehouseId;
        this.status = status;
        this.expectedArrivalAt = expectedArrivalAt;
        this.cancelReason = cancelReason;
        this.version = version;
    }

    public static InboundOrderAggregate create(long id, String inboundNo, String sourceType, String sourceNo,
                                               long warehouseId, OffsetDateTime expectedArrivalAt) {
        return new InboundOrderAggregate(id, inboundNo, sourceType, sourceNo, warehouseId,
                InboundOrderStatus.PENDING_ARRIVAL, expectedArrivalAt, null, 0);
    }

    public void cancel(String reason) {
        if (status == InboundOrderStatus.RECEIVING || status == InboundOrderStatus.RECEIVED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "入库单已开始收货，不能取消");
        }
        if (status == InboundOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "入库单已取消");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "取消原因不能为空");
        }
        status = InboundOrderStatus.CANCELLED;
        cancelReason = reason;
        version++;
    }

    public long id() { return id; }
    public String inboundNo() { return inboundNo; }
    public String sourceType() { return sourceType; }
    public String sourceNo() { return sourceNo; }
    public long warehouseId() { return warehouseId; }
    public InboundOrderStatus status() { return status; }
    public OffsetDateTime expectedArrivalAt() { return expectedArrivalAt; }
    public String cancelReason() { return cancelReason; }
    public int version() { return version; }
}
