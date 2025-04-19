package org.scm.pms.application.command;

public record AuditReturnSupplyApplyCommand(
        String applyNo,
        boolean approved
) {}