package org.scm.pms.application.command;

public record CreateReturnSupplyDeliveryCommand(
        String deliveryNo,
        String orderNo,
        Integer supplierId,
        Integer warehouseId,
        Integer logisticChannelId,
        String logisticNo
) {}