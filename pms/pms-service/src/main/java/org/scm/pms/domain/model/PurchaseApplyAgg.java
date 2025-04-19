package org.scm.pms.domain.model;

import org.scm.common.exception.BizException;
import org.scm.common.exception.PurchaseErrorCode;
import org.scm.pms._share.enums.PurchaseApplyStatus;
import org.scm.pms.application.command.SubmitPurchaseApplyCommand;

import java.math.BigDecimal;

public class PurchaseApplyAgg {

    private Long id;
    private String applyNo;
    private Long applyEmpId;
    private Long purchaserEmpId;
    private BigDecimal estimatedTotalPrice;
    private PurchaseApplyStatus status;

    public static PurchaseApplyAgg submit(SubmitPurchaseApplyCommand command) {
        PurchaseApplyAgg agg = new PurchaseApplyAgg();
        agg.applyNo = command.applyNo();
        agg.applyEmpId = command.applyEmpId();
        agg.purchaserEmpId = command.purchaserEmpId();
        agg.estimatedTotalPrice = command.estimatedTotalPrice();
        agg.status = PurchaseApplyStatus.SUBMITTED;
        return agg;
    }

    public void audit(Boolean approved, String reason) {
        if (status != PurchaseApplyStatus.SUBMITTED) {
            throw new BizException(PurchaseErrorCode.INVALID_STATUS);
        }
        status = approved ? PurchaseApplyStatus.AUDITED : PurchaseApplyStatus.REJECTED;
    }

    public void cancel() {
        if (status == PurchaseApplyStatus.AUDITED) {
            throw new BizException(PurchaseErrorCode.INVALID_STATUS);
        }
        status = PurchaseApplyStatus.CANCELLED;
    }

    public Long getId() { return this.id; }
    public String getApplyNo() { return this.applyNo; }
    public PurchaseApplyStatus getStatus() { return this.status; }
}
