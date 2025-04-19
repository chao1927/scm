package org.scm.srm.wms.application.command;

public record ReceiveReturnInboundCommand(
        String inboundNo,
        String salesReturnNo,
        String warehouseId,
        Long operatorId,
        String logisticsNo
) {}