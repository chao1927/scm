package com.chaobo.scm.supplier.application.shared;

public record CommandResult(long aggregateId, String businessNo, int status, String statusName,
                            int version, String eventCode, boolean idempotentHit) {
    public CommandResult asIdempotentHit() {
        return new CommandResult(aggregateId, businessNo, status, statusName, version, eventCode, true);
    }
}
