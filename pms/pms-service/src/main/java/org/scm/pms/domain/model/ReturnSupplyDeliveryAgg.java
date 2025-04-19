package org.scm.pms.domain.model;

import org.scm.pms.adapter.infra.domain.ReturnSupplyDeliveryOrder;
import org.scm.pms.application.command.CreateReturnSupplyDeliveryCommand;

import java.time.LocalDateTime;

public record ReturnSupplyDeliveryAgg(ReturnSupplyDeliveryOrder delivery) {

    public static ReturnSupplyDeliveryAgg create(CreateReturnSupplyDeliveryCommand command) {
        ReturnSupplyDeliveryOrder order = new ReturnSupplyDeliveryOrder();
        order.setDeliveryNo(command.deliveryNo());
        order.setOrderNo(command.orderNo());
        order.setSupplierId(command.supplierId());
        order.setWarehouseId(command.warehouseId());
        order.setDeliveryTime(LocalDateTime.now());
        order.setLogisticChannelId(command.logisticChannelId());
        order.setLogisticNo(command.logisticNo());
        order.setStatus(1); // 待发货
        return new ReturnSupplyDeliveryAgg(order);
    }

    public void dispatch() {
        delivery.setStatus(2); // 已发货
        delivery.setDeliveryTime(LocalDateTime.now());
    }

    public void markInTransit() {
        delivery.setStatus(3); // 运输中
    }

    public void confirmReceived() {
        delivery.setStatus(4); // 已收货
    }

    public ReturnSupplyDeliveryOrder entity() {
        return delivery;
    }
}
