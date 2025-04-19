package org.scm.pms.application.command;

public record SubmitReturnSupplyApplyCommand(
        String applyNo,
        Integer salesEmpId,
        String strategyConfig,
        Integer totalItemTypes,
        Integer totalQuantity,
        String remark
) {}