package org.scm.srm.wms.application.command;

public record StartReturnSupplyOutboundCommand(
        String outboundNo,
        String deliveryNo,
        String orderNo,
        Integer warehouseId,
        Integer operatorId
) {}