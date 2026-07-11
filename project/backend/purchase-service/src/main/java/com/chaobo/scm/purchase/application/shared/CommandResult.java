package com.chaobo.scm.purchase.application.shared;

public record CommandResult(
        long id,
        String businessNo,
        int status,
        String statusName,
        int version,
        String eventCode,
        boolean duplicated) {
}
