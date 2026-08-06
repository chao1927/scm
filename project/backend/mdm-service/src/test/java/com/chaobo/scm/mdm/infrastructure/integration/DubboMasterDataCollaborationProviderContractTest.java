package com.chaobo.scm.mdm.infrastructure.integration;

import com.chaobo.scm.common.integration.MasterDataCollaborationApi;
import com.chaobo.scm.common.integration.ScmDubboContract;
import org.apache.dubbo.config.annotation.DubboService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 MDM Provider 对外发布的稳定 Dubbo 元数据。 */
class DubboMasterDataCollaborationProviderContractTest {
    @Test
    void shouldPublishStableContract() {
        assertThat(MasterDataCollaborationApi.class).isAssignableFrom(DubboMasterDataCollaborationProvider.class);
        DubboService service = DubboMasterDataCollaborationProvider.class.getAnnotation(DubboService.class);
        assertThat(service).isNotNull();
        assertThat(service.group()).isEqualTo(ScmDubboContract.GROUP);
        assertThat(service.version()).isEqualTo(ScmDubboContract.VERSION);
        assertThat(service.timeout()).isEqualTo(ScmDubboContract.TIMEOUT_MILLIS);
        assertThat(service.retries()).isZero();
    }
}
