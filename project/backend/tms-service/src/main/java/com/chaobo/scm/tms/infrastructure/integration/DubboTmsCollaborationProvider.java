package com.chaobo.scm.tms.infrastructure.integration;

import com.chaobo.scm.common.integration.ScmDubboContract;
import com.chaobo.scm.common.integration.TmsCollaborationApi;
import com.chaobo.scm.tms.application.TmsCollaborationApplicationService;
import org.apache.dubbo.config.annotation.DubboService;

/** TMS 同步运输协作的真实 Dubbo Provider。 */
@DubboService(group = ScmDubboContract.GROUP, version = ScmDubboContract.VERSION,
        protocol = "tri", timeout = ScmDubboContract.TIMEOUT_MILLIS, retries = 0)
public class DubboTmsCollaborationProvider implements TmsCollaborationApi {

    private final TmsCollaborationApplicationService service;

    public DubboTmsCollaborationProvider(TmsCollaborationApplicationService service) {
        this.service = service;
    }

    @Override
    public TransportResult createInboundTransport(InboundTransportCommand command) {
        return service.createInbound(command);
    }

    @Override
    public TransportResult createSupplierReturnTransport(ReturnTransportCommand command) {
        return service.createReturn(command);
    }

    @Override
    public void cancelTransport(CancelTransportCommand command) {
        service.cancel(command);
    }
}
