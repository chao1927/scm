package org.scm.pms.domain.model;

import org.scm.pms.adapter.infra.domain.ReturnSupplyApply;
import org.scm.pms.application.command.SubmitReturnSupplyApplyCommand;
import java.time.LocalDateTime;

public record ReturnSupplyApplyAgg(ReturnSupplyApply apply) {

    public static ReturnSupplyApplyAgg create(SubmitReturnSupplyApplyCommand command) {
        ReturnSupplyApply apply = new ReturnSupplyApply();
        apply.setApplyNo(command.applyNo());
        apply.setSalesEmpId(command.salesEmpId());
        apply.setStrategyConfig(command.strategyConfig());
        apply.setApplyTime(LocalDateTime.now());
        apply.setStatus(1); // 待审核
        apply.setTotalItemTypes(command.totalItemTypes());
        apply.setTotalQuantity(command.totalQuantity());
        apply.setRemark(command.remark());
        return new ReturnSupplyApplyAgg(apply);
    }

    public void audit(boolean approved) {
        apply.setStatus(approved ? 2 : 3); // 2:审核通过，3:拒绝
    }

    public void cancel() {
        apply.setStatus(4); // 取消状态
    }

    public ReturnSupplyApply entity() {
        return apply;
    }
}
