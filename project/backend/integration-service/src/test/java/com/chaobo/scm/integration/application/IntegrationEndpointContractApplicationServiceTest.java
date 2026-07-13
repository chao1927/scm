package com.chaobo.scm.integration.application;

import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationEndpointContractApplicationServiceTest {
    @Test
    void verifiesRocketMqEndpointContract() {
        IntegrationEndpointContractApplicationService service =
                new IntegrationEndpointContractApplicationService(null);
        IntegrationMapper.EndpointRow endpoint = new IntegrationMapper.EndpointRow(null, "IE1", "BMS",
                "ROCKETMQ", "rocketmq://mq-ns:9876/scm.bms.events?tag=Fee", 3000, 3, 0, 1, 1);

        IntegrationEndpointContractApplicationService.EndpointVerificationResult result = service.verify(endpoint);

        assertThat(result.valid()).isTrue();
        assertThat(result.metadata()).containsEntry("nameserver", "mq-ns:9876")
                .containsEntry("topic", "scm.bms.events")
                .containsEntry("tag", "Fee");
    }

    @Test
    void verifiesDubboEndpointContract() {
        IntegrationEndpointContractApplicationService service =
                new IntegrationEndpointContractApplicationService(null);
        IntegrationMapper.EndpointRow endpoint = new IntegrationMapper.EndpointRow(null, "IE2", "INVENTORY",
                "DUBBO", "dubbo://com.chaobo.scm.inventory.InventoryCommandFacade/reserve?version=1.0.0",
                2000, 3, 0, 1, 1);

        IntegrationEndpointContractApplicationService.EndpointVerificationResult result = service.verify(endpoint);

        assertThat(result.valid()).isTrue();
        assertThat(result.metadata()).containsEntry("serviceInterface",
                        "com.chaobo.scm.inventory.InventoryCommandFacade")
                .containsEntry("method", "reserve")
                .containsEntry("version", "1.0.0");
    }

    @Test
    void verifiesHttpEndpointContractAndRejectsInvalidMqEndpoint() {
        IntegrationEndpointContractApplicationService service =
                new IntegrationEndpointContractApplicationService(null);
        IntegrationMapper.EndpointRow http = new IntegrationMapper.EndpointRow(null, "IE3", "WMS", "HTTP",
                "https://wms.internal/events", 1000, 3, 0, 1, 1);
        IntegrationMapper.EndpointRow invalidMq = new IntegrationMapper.EndpointRow(null, "IE4", "BMS",
                "ROCKETMQ", "http://mq/scm.bms.events", 3000, 3, 0, 1, 1);

        assertThat(service.verify(http).valid()).isTrue();
        assertThat(service.verify(invalidMq).valid()).isFalse();
        assertThat(service.verify(invalidMq).message()).contains("rocketmq scheme");
    }
}
