package org.scm.oms.application.command;

public record AuditReturnApplyCommand(
        String returnApplyNo,
        Boolean approved
) {}