package org.scm.pms.domain.model;

import org.scm.pms.adapter.infra.domain.ReturnSupplyOrder;
import org.scm.pms.application.command.CreateReturnSupplyOrderCommand;

import java.time.LocalDateTime;

public record ReturnSupplyOrderAgg(ReturnSupplyOrder order) {

    public static ReturnSupplyOrderAgg create(CreateReturnSupplyOrderCommand command) {
        ReturnSupplyOrder order = new ReturnSupplyOrder();
        order.setOrderNo(command.orderNo());
        order.setApplyNo(command.applyNo());
        order.setSupplierId(command.supplierId());
        order.setInitiateTime(LocalDateTime.now());
        order.setStatus(1); // 待确认
        order.setTotalItemTypes(command.totalItemTypes());
        order.setTotalQuantity(command.totalQuantity());
        order.setRemark(command.remark());
        return new ReturnSupplyOrderAgg(order);
    }

    public void confirm() {
        order.setStatus(2); // 已确认
    }

    public void cancel() {
        order.setStatus(3); // 已取消
    }

    public ReturnSupplyOrder entity() {
        return order;
    }
}
