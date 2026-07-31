package com.chaobo.scm.wms.domain.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/** 统一入库单聚合根，保护多类型来源唯一性和接单不变量。 */
public class InboundOrderAggregate {

    private final long id;
    private final String inboundNo;
    private final String sourceSystem;
    private final InboundType inboundType;
    private final String sourceNo;
    private final String sourceLineNo;
    private final long warehouseId;
    private final long ownerId;
    private final BigDecimal allowedQty;
    private InboundOrderStatus status;
    private OffsetDateTime expectedArrivalAt;
    private String cancelReason;
    private int version;

    public InboundOrderAggregate(long id, String inboundNo, String sourceSystem, String inboundType,
                                 String sourceNo, String sourceLineNo, long warehouseId, long ownerId,
                                 BigDecimal allowedQty, InboundOrderStatus status,
                                 OffsetDateTime expectedArrivalAt, String cancelReason, int version) {
        InboundType normalizedType = InboundType.fromExternal(inboundType);
        normalizedType.requireSourceSystem(sourceSystem);
        if (blank(inboundNo) || blank(sourceNo) || blank(sourceLineNo) || warehouseId <= 0 || ownerId <= 0
            || allowedQty == null || allowedQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                "入库单号、来源引用、仓库、货主和允许收货量必须合法");
        }
        this.id = id;
        this.inboundNo = inboundNo;
        this.sourceSystem = sourceSystem.trim().toUpperCase();
        this.inboundType = normalizedType;
        this.sourceNo = sourceNo;
        this.sourceLineNo = sourceLineNo;
        this.warehouseId = warehouseId;
        this.ownerId = ownerId;
        this.allowedQty = allowedQty;
        this.status = Objects.requireNonNull(status, "status");
        this.expectedArrivalAt = expectedArrivalAt;
        this.cancelReason = cancelReason;
        this.version = version;
    }

    public static InboundOrderAggregate create(long id, String inboundNo, String sourceSystem,
                                                String inboundType, String sourceNo, String sourceLineNo,
                                                long warehouseId, long ownerId, BigDecimal allowedQty,
                                                OffsetDateTime expectedArrivalAt) {
        return new InboundOrderAggregate(id, inboundNo, sourceSystem, inboundType, sourceNo, sourceLineNo,
            warehouseId, ownerId, allowedQty, InboundOrderStatus.PENDING_ARRIVAL,
            expectedArrivalAt, null, 0);
    }

    /** 重复事实只有在接单快照完全一致时才是无副作用幂等。 */
    public boolean hasSameDefinition(String sourceSystem, String inboundType, String sourceNo,
                                     String sourceLineNo, long warehouseId, long ownerId,
                                     BigDecimal allowedQty) {
        return this.sourceSystem.equalsIgnoreCase(sourceSystem)
            && this.inboundType == InboundType.fromExternal(inboundType)
            && this.sourceNo.equals(sourceNo)
            && this.sourceLineNo.equals(sourceLineNo)
            && this.warehouseId == warehouseId
            && this.ownerId == ownerId
            && this.allowedQty.compareTo(allowedQty) == 0;
    }

    public void cancel(String reason) {
        if (status == InboundOrderStatus.RECEIVING || status == InboundOrderStatus.RECEIVED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "入库单已开始收货，不能取消");
        }
        if (status == InboundOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "入库单已取消");
        }
        if (blank(reason)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "取消原因不能为空");
        }
        status = InboundOrderStatus.CANCELLED;
        cancelReason = reason;
        version++;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public long id() { return id; }
    public String inboundNo() { return inboundNo; }
    public String sourceSystem() { return sourceSystem; }
    public String sourceType() { return inboundType.name(); }
    public InboundType inboundType() { return inboundType; }
    public String sourceNo() { return sourceNo; }
    public String sourceLineNo() { return sourceLineNo; }
    public long warehouseId() { return warehouseId; }
    public long ownerId() { return ownerId; }
    public BigDecimal allowedQty() { return allowedQty; }
    public InboundOrderStatus status() { return status; }
    public OffsetDateTime expectedArrivalAt() { return expectedArrivalAt; }
    public String cancelReason() { return cancelReason; }
    public int version() { return version; }
}
