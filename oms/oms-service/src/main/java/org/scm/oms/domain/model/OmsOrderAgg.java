package org.scm.oms.domain.model;

import org.scm.oms.adapter.infra.domain.OmsOrder;
import org.scm.oms.application.command.CreateOmsOrderCommand;

public record OmsOrderAgg(OmsOrder order) {

    public static OmsOrderAgg create(CreateOmsOrderCommand cmd) {
        // TODO 实现创建逻辑
        return null;
    }

    public void audit() {
        order.setOrderStatus(2); // 审核通过
    }

    public void allocateWarehouse() {
        order.setOrderStatus(3);
    }

    public void splitOrder() {
        order.setOrderStatus(4);
    }

    public void updateStatus(int status) {
        order.setOrderStatus(status);
    }

}
