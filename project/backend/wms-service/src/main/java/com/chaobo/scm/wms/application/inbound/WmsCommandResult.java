package com.chaobo.scm.wms.application.inbound;

public record WmsCommandResult(long id, String inboundNo, int status, String statusName, int version,
                               boolean duplicated) {
}
