package com.chaobo.scm.wms.domain.receiving;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;

public class ReceiptAggregate {
    private final long id;
    private final String receiptNo;
    private final long inboundId;
    private final String skuCode;
    private final BigDecimal expectedQty;
    private BigDecimal receivedQty;
    private BigDecimal rejectedQty;
    private ReceiptStatus status;
    private int version;

    public ReceiptAggregate(long id, String receiptNo, long inboundId, String skuCode, BigDecimal expectedQty,
                            BigDecimal receivedQty, BigDecimal rejectedQty, ReceiptStatus status, int version) {
        if (inboundId <= 0 || skuCode == null || skuCode.isBlank() || expectedQty == null || expectedQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "收货单来源和通知数量不能为空");
        }
        this.id = id;
        this.receiptNo = receiptNo;
        this.inboundId = inboundId;
        this.skuCode = skuCode;
        this.expectedQty = expectedQty;
        this.receivedQty = zero(receivedQty);
        this.rejectedQty = zero(rejectedQty);
        this.status = status;
        this.version = version;
    }

    public void scan(BigDecimal received, BigDecimal rejected, String rejectReason) {
        ensureReceiving();
        if (received == null || rejected == null || received.signum() < 0 || rejected.signum() < 0
                || received.add(rejected).signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "扫码收货数量不合法");
        }
        if (rejected.signum() > 0 && (rejectReason == null || rejectReason.isBlank())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "拒收必须填写原因");
        }
        if (receivedQty.add(rejectedQty).add(received).add(rejected).compareTo(expectedQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "收货数量不能超过通知数量");
        }
        receivedQty = receivedQty.add(received);
        rejectedQty = rejectedQty.add(rejected);
        version++;
    }

    public void complete() {
        ensureReceiving();
        if (receivedQty.add(rejectedQty).compareTo(expectedQty) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "收货数量与通知数量不一致");
        }
        status = ReceiptStatus.COMPLETED;
        version++;
    }

    private void ensureReceiving() {
        if (status != ReceiptStatus.RECEIVING) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "收货单当前不可操作");
        }
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public long id() {
        return id;
    }

    public String receiptNo() {
        return receiptNo;
    }

    public long inboundId() {
        return inboundId;
    }

    public String skuCode() {
        return skuCode;
    }

    public BigDecimal expectedQty() {
        return expectedQty;
    }

    public BigDecimal receivedQty() {
        return receivedQty;
    }

    public BigDecimal rejectedQty() {
        return rejectedQty;
    }

    public ReceiptStatus status() {
        return status;
    }

    public int version() {
        return version;
    }
}
