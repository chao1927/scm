package org.scm.oms.application.command;

public record CreateDeliveryOrderCommand(
        String deliveryNo,
        String omsOrderNo,
        Long warehouseId
) {}