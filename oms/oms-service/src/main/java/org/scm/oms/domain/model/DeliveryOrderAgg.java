package org.scm.oms.domain.model;

import org.scm.oms.adapter.infra.domain.DeliveryOrder;
import org.scm.oms.application.command.CreateDeliveryOrderCommand;

public record DeliveryOrderAgg(DeliveryOrder deliveryOrder) {


    public static DeliveryOrderAgg create(CreateDeliveryOrderCommand command) {
        // TODO 实现创建逻辑
        return null;
    }

    public void updateStatus(Integer status) {
        this.deliveryOrder.setDeliveryStatus(status);
    }

    public void assignLogistics(String logisticNo, Long channelId) {
        deliveryOrder.setLogisticNo(logisticNo);
        deliveryOrder.setLogisticChannelId(channelId);
    }
}
