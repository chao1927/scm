package org.scm.srm.wms.application.command;

public record CreateSalesOutboundOrderCommand(
        String outboundNo,
        String deliveryNo,
        String waveNo,
        String sortingNo,
        String warehouseId
) {}