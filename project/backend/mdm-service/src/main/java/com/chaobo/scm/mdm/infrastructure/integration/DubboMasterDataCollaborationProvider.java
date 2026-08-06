package com.chaobo.scm.mdm.infrastructure.integration;

import com.chaobo.scm.common.integration.MasterDataCollaborationApi;
import com.chaobo.scm.common.integration.ScmDubboContract;
import com.chaobo.scm.mdm.application.MdmCollaborationApplicationService;
import org.apache.dubbo.config.annotation.DubboService;

/** MDM 供应商建档与生命周期的真实 Dubbo Provider。 */
@DubboService(group = ScmDubboContract.GROUP, version = ScmDubboContract.VERSION,
        protocol = "tri", timeout = ScmDubboContract.TIMEOUT_MILLIS, retries = 0)
public class DubboMasterDataCollaborationProvider implements MasterDataCollaborationApi {

    private final MdmCollaborationApplicationService service;

    public DubboMasterDataCollaborationProvider(MdmCollaborationApplicationService service) {
        this.service = service;
    }

    @Override
    public SupplierResult createSupplier(CreateSupplierCommand command) {
        return service.create(command);
    }

    @Override
    public void changeSupplierStatus(ChangeSupplierStatusCommand command) {
        service.changeStatus(command);
    }
}
