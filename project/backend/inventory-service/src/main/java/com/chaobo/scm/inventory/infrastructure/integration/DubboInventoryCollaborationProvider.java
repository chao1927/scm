package com.chaobo.scm.inventory.infrastructure.integration;

import com.chaobo.scm.common.integration.InventoryCollaborationApi;
import com.chaobo.scm.common.integration.ScmDubboContract;
import com.chaobo.scm.inventory.application.InventoryCollaborationApplicationService;
import org.apache.dubbo.config.annotation.DubboService;

/** 中央库存退供锁定的真实 Dubbo Provider。 */
@DubboService(group = ScmDubboContract.GROUP, version = ScmDubboContract.VERSION,
        protocol = "tri", timeout = ScmDubboContract.TIMEOUT_MILLIS, retries = 0)
public class DubboInventoryCollaborationProvider implements InventoryCollaborationApi {

    private final InventoryCollaborationApplicationService service;

    public DubboInventoryCollaborationProvider(InventoryCollaborationApplicationService service) {
        this.service = service;
    }

    @Override
    public LockResult lockSupplierReturn(ReturnLockCommand command) {
        return service.lock(command);
    }

    @Override
    public void releaseSupplierReturn(ReturnReleaseCommand command) {
        service.release(command);
    }
}
