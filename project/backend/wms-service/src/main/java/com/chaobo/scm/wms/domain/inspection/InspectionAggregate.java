package com.chaobo.scm.wms.domain.inspection;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;

public class InspectionAggregate {
    private final long id;
    private final String inspectionNo;
    private final long receiptId;
    private final BigDecimal inspectQty;
    private BigDecimal qualifiedQty = BigDecimal.ZERO;
    private BigDecimal unqualifiedQty = BigDecimal.ZERO;
    private boolean completed;
    private int version;

    public InspectionAggregate(long id, String inspectionNo, long receiptId, BigDecimal inspectQty) {
        if (receiptId <= 0 || inspectQty == null || inspectQty.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "质检来源和数量不合法");
        }
        this.id = id;
        this.inspectionNo = inspectionNo;
        this.receiptId = receiptId;
        this.inspectQty = inspectQty;
    }

    public static InspectionAggregate rehydrate(
            long id,
            String no,
            long receiptId,
            BigDecimal qty,
            BigDecimal qualified,
            BigDecimal unqualified,
            boolean completed,
            int version
    ) {
        var inspection = new InspectionAggregate(id, no, receiptId, qty);
        inspection.qualifiedQty = qualified;
        inspection.unqualifiedQty = unqualified;
        inspection.completed = completed;
        inspection.version = version;
        return inspection;
    }

    public void submit(BigDecimal qualified, BigDecimal unqualified) {
        if (completed) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "质检单已完成");
        }
        if (qualified == null || unqualified == null || qualified.signum() < 0 || unqualified.signum() < 0
                || qualified.add(unqualified).compareTo(inspectQty) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "合格和不合格数量之和必须等于质检数量");
        }
        qualifiedQty = qualified;
        unqualifiedQty = unqualified;
        completed = true;
        version++;
    }

    public long id() {
        return id;
    }

    public String inspectionNo() {
        return inspectionNo;
    }

    public long receiptId() {
        return receiptId;
    }

    public BigDecimal inspectQty() {
        return inspectQty;
    }

    public BigDecimal qualifiedQty() {
        return qualifiedQty;
    }

    public BigDecimal unqualifiedQty() {
        return unqualifiedQty;
    }

    public boolean completed() {
        return completed;
    }

    public int version() {
        return version;
    }
}
