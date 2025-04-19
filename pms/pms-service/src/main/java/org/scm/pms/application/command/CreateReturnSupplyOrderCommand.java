package org.scm.pms.application.command;

public record CreateReturnSupplyOrderCommand(
        String orderNo,
        String applyNo,
        Integer supplierId,
        Integer totalItemTypes,
        Integer totalQuantity,
        String remark
) {}