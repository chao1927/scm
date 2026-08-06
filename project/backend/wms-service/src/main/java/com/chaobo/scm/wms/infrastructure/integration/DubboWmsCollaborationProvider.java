package com.chaobo.scm.wms.infrastructure.integration;

import com.chaobo.scm.common.integration.ScmDubboContract;
import com.chaobo.scm.common.integration.WmsCollaborationApi;
import com.chaobo.scm.wms.application.integration.WmsCollaborationApplicationService;
import org.apache.dubbo.config.annotation.DubboService;

/** WMS 同步协作契约的真实 Dubbo Provider。 */
@DubboService(group = ScmDubboContract.GROUP, version = ScmDubboContract.VERSION,
        protocol = "tri", timeout = ScmDubboContract.TIMEOUT_MILLIS, retries = 0)
public class DubboWmsCollaborationProvider implements WmsCollaborationApi {

    private final WmsCollaborationApplicationService service;

    public DubboWmsCollaborationProvider(WmsCollaborationApplicationService service) {
        this.service = service;
    }

    @Override
    public AppointmentResult createOrAdjustInboundAppointment(InboundAppointmentCommand command) {
        return service.createAppointment(command);
    }

    @Override
    public void cancelInboundAppointment(CancelAppointmentCommand command) {
        service.cancelAppointment(command);
    }

    @Override
    public OutboundResult createSupplierReturnOutbound(ReturnOutboundCommand command) {
        return service.createReturnOutbound(command);
    }
}
