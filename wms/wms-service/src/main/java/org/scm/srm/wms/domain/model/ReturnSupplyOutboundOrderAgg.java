package org.scm.srm.wms.domain.model;


import org.scm.srm.wms.adapter.infra.domain.ReturnSupplyOutboundOrder;
import org.scm.srm.wms.application.command.StartReturnSupplyOutboundCommand;

import java.time.LocalDateTime;

public record ReturnSupplyOutboundOrderAgg(ReturnSupplyOutboundOrder outboundOrder) {

    public static ReturnSupplyOutboundOrderAgg create(StartReturnSupplyOutboundCommand command) {
        ReturnSupplyOutboundOrder order = new ReturnSupplyOutboundOrder();
        order.setOutboundNo(command.outboundNo());
        order.setDeliveryNo(command.deliveryNo());
        order.setOrderNo(command.orderNo());
        order.setWarehouseId(command.warehouseId());
        order.setOperatorId(command.operatorId());
        order.setStartTime(LocalDateTime.now());
        order.setStatus(1); // 1 - 待出库
        return new ReturnSupplyOutboundOrderAgg(order);
    }

    public void completeOutbound() {
        outboundOrder.setStatus(2); // 2 - 已出库
        outboundOrder.setOutboundTime(LocalDateTime.now());
    }

    public ReturnSupplyOutboundOrder entity() {
        return outboundOrder;
    }
}
