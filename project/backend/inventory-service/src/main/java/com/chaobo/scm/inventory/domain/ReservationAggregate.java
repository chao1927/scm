package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;

public class ReservationAggregate {
    private final long id;
    private final String reservationNo;
    private final long accountId;
    private final String sourceSystem;
    private final String sourceNo;
    private final BigDecimal reservedQty;
    private BigDecimal releasedQty;
    private int status;
    private int version;

    public ReservationAggregate(long id, String reservationNo, long accountId, String sourceSystem, String sourceNo,
                                BigDecimal reservedQty, BigDecimal releasedQty, int status, int version) {
        if (reservationNo == null || reservationNo.isBlank() || accountId <= 0 || sourceSystem == null
                || sourceSystem.isBlank() || sourceNo == null || sourceNo.isBlank()
                || reservedQty == null || reservedQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预占单数据不合法");
        }
        this.id = id;
        this.reservationNo = reservationNo;
        this.accountId = accountId;
        this.sourceSystem = sourceSystem;
        this.sourceNo = sourceNo;
        this.reservedQty = reservedQty;
        this.releasedQty = releasedQty == null ? BigDecimal.ZERO : releasedQty;
        this.status = status;
        this.version = version;
    }

    public BigDecimal releaseAll() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "预占当前不可释放");
        }
        BigDecimal qty = reservedQty.subtract(releasedQty);
        if (qty.signum() <= 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "预占已无可释放数量");
        }
        releasedQty = reservedQty;
        status = 2;
        version++;
        return qty;
    }

    public void close() {
        if (status == 3) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "预占已关闭");
        }
        status = 3;
        version++;
    }

    public long id() { return id; }
    public String reservationNo() { return reservationNo; }
    public long accountId() { return accountId; }
    public String sourceSystem() { return sourceSystem; }
    public String sourceNo() { return sourceNo; }
    public BigDecimal reservedQty() { return reservedQty; }
    public BigDecimal releasedQty() { return releasedQty; }
    public int status() { return status; }
    public int version() { return version; }
}
