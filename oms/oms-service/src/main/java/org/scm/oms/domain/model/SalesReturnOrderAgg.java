package org.scm.oms.domain.model;

import org.scm.oms.adapter.infra.domain.SalesReturnOrder;
import org.scm.oms.application.command.CreateSalesReturnOrderCommand;
import org.scm.oms.application.command.UpdateReturnStatusCommand;

public record SalesReturnOrderAgg(SalesReturnOrder order) {

    public static SalesReturnOrderAgg create(CreateSalesReturnOrderCommand command) {
        SalesReturnOrder entity = new SalesReturnOrder();
        entity.setSalesReturnNo(command.salesReturnNo());
        entity.setOrderNo(command.orderNo());
        entity.setReturnApplyNo(command.returnApplyNo());
        entity.setRefundReason(command.refundReason());
        entity.setReturnType(command.returnType());
        entity.setLogisticsNo(command.logisticsNo());
        return new SalesReturnOrderAgg(entity);
    }

    public void updateStatus(int status) {
        // TODO 验证状态是否合法
    }
}
