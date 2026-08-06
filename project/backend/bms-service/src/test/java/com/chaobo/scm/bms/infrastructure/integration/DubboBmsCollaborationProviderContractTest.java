package com.chaobo.scm.bms.infrastructure.integration;

import com.chaobo.scm.common.integration.BmsCollaborationApi;
import com.chaobo.scm.common.integration.ScmDubboContract;
import org.apache.dubbo.config.annotation.DubboService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 BMS Provider 对外发布的稳定 Dubbo 元数据。 */
class DubboBmsCollaborationProviderContractTest {
    @Test
    void shouldPublishStableContract() {
        assertThat(BmsCollaborationApi.class).isAssignableFrom(DubboBmsCollaborationProvider.class);
        DubboService service = DubboBmsCollaborationProvider.class.getAnnotation(DubboService.class);
        assertThat(service).isNotNull();
        assertThat(service.group()).isEqualTo(ScmDubboContract.GROUP);
        assertThat(service.version()).isEqualTo(ScmDubboContract.VERSION);
        assertThat(service.timeout()).isEqualTo(ScmDubboContract.TIMEOUT_MILLIS);
        assertThat(service.retries()).isZero();
    }
}
