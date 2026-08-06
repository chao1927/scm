package com.chaobo.scm.iam.infrastructure.integration;

import com.chaobo.scm.common.integration.IamCollaborationApi;
import com.chaobo.scm.common.integration.ScmDubboContract;
import com.chaobo.scm.iam.application.IamCollaborationApplicationService;
import org.apache.dubbo.config.annotation.DubboService;

/** IAM 用户供应商数据范围的真实 Dubbo Provider。 */
@DubboService(group = ScmDubboContract.GROUP, version = ScmDubboContract.VERSION,
        protocol = "tri", timeout = ScmDubboContract.TIMEOUT_MILLIS, retries = 0)
public class DubboIamCollaborationProvider implements IamCollaborationApi {

    private final IamCollaborationApplicationService service;

    public DubboIamCollaborationProvider(IamCollaborationApplicationService service) {
        this.service = service;
    }

    @Override
    public void updateSupplierDataScope(UpdateSupplierScopeCommand command) {
        service.updateSupplierScope(command);
    }
}
