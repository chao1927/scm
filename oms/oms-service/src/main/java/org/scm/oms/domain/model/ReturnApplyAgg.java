package org.scm.oms.domain.model;

import org.scm.oms.adapter.infra.domain.ReturnApply;
import org.scm.oms.application.command.SubmitReturnApplyCommand;

public record ReturnApplyAgg(ReturnApply returnApply) {


    public static ReturnApplyAgg create(SubmitReturnApplyCommand command) {
        // TODO 实现创建逻辑
        return null;
    }

    public void audit(boolean approved) {
        // TODO: 2023/4/16 待实现
    }

}
