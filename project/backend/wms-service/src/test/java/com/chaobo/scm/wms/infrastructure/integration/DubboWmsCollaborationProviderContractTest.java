package com.chaobo.scm.wms.infrastructure.integration;

import com.chaobo.scm.common.integration.ScmDubboContract;
import com.chaobo.scm.common.integration.WmsCollaborationApi;
import org.apache.dubbo.config.annotation.DubboService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 WMS Provider 对外发布的稳定 Dubbo 元数据。 */
class DubboWmsCollaborationProviderContractTest {
    @Test
    void shouldPublishStableContract() {
        assertThat(WmsCollaborationApi.class).isAssignableFrom(DubboWmsCollaborationProvider.class);
        DubboService service = DubboWmsCollaborationProvider.class.getAnnotation(DubboService.class);
        assertThat(service).isNotNull();
        assertThat(service.group()).isEqualTo(ScmDubboContract.GROUP);
        assertThat(service.version()).isEqualTo(ScmDubboContract.VERSION);
        assertThat(service.timeout()).isEqualTo(ScmDubboContract.TIMEOUT_MILLIS);
        assertThat(service.retries()).isZero();
    }
}
