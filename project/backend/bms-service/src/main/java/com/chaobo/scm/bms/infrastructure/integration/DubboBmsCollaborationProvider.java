package com.chaobo.scm.bms.infrastructure.integration;

import com.chaobo.scm.bms.application.BmsCollaborationApplicationService;
import com.chaobo.scm.common.integration.BmsCollaborationApi;
import com.chaobo.scm.common.integration.ScmDubboContract;
import org.apache.dubbo.config.annotation.DubboService;

/** BMS 退供结算的真实 Dubbo Provider。 */
@DubboService(group = ScmDubboContract.GROUP, version = ScmDubboContract.VERSION,
        protocol = "tri", timeout = ScmDubboContract.TIMEOUT_MILLIS, retries = 0)
public class DubboBmsCollaborationProvider implements BmsCollaborationApi {

    private final BmsCollaborationApplicationService service;

    public DubboBmsCollaborationProvider(BmsCollaborationApplicationService service) {
        this.service = service;
    }

    @Override
    public SettlementResult createSupplierReturnSettlement(ReturnSettlementCommand command) {
        return service.create(command);
    }
}
