package com.chaobo.scm.inventory.infrastructure.integration;

import com.chaobo.scm.common.integration.InventoryCollaborationApi;
import com.chaobo.scm.common.integration.ScmDubboContract;
import org.apache.dubbo.config.annotation.DubboService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证中央库存 Provider 对外发布的稳定 Dubbo 元数据。 */
class DubboInventoryCollaborationProviderContractTest {
    @Test
    void shouldPublishStableContract() {
        assertThat(InventoryCollaborationApi.class).isAssignableFrom(DubboInventoryCollaborationProvider.class);
        DubboService service = DubboInventoryCollaborationProvider.class.getAnnotation(DubboService.class);
        assertThat(service).isNotNull();
        assertThat(service.group()).isEqualTo(ScmDubboContract.GROUP);
        assertThat(service.version()).isEqualTo(ScmDubboContract.VERSION);
        assertThat(service.timeout()).isEqualTo(ScmDubboContract.TIMEOUT_MILLIS);
        assertThat(service.retries()).isZero();
    }
}
