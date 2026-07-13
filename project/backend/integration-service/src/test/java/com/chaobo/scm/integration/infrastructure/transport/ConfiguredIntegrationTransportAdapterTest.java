package com.chaobo.scm.integration.infrastructure.transport;

import com.chaobo.scm.integration.application.IntegrationTransportPort;
import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
import com.chaobo.scm.integration.application.IntegrationApplicationServiceTest;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguredIntegrationTransportAdapterTest {
    @Test
    void buildsHttpRequestWithContractHeaders() {
        IntegrationApplicationServiceTest.MemoryIntegrationMapper mapper =
                new IntegrationApplicationServiceTest.MemoryIntegrationMapper();
        ConfiguredIntegrationTransportAdapter adapter = new ConfiguredIntegrationTransportAdapter(mapper);
        IntegrationMapper.EndpointRow endpoint = new IntegrationMapper.EndpointRow(null, "IE1", "WMS", "HTTP",
                "http://127.0.0.1:18080/events", 1000, 3, 0, 1, 1);

        HttpRequest request = adapter.buildHttpRequest(endpoint,
                new IntegrationTransportPort.DeliveryRequest("IM1", "PurchaseOrderReleased", "PURCHASE",
                        "WMS", "HTTP", "PO-1", "idem-1", "{\"po\":\"PO-1\"}"));

        assertThat(request.uri().toString()).isEqualTo("http://127.0.0.1:18080/events");
        assertThat(request.timeout()).hasValueSatisfying(timeout -> assertThat(timeout.toMillis()).isEqualTo(1000));
        assertThat(request.headers().firstValue("X-Integration-Message-No")).contains("IM1");
        assertThat(request.headers().firstValue("X-Idempotency-Key")).contains("idem-1");
        assertThat(request.headers().firstValue("X-Source-System")).contains("PURCHASE");
        assertThat(request.headers().firstValue("X-Target-System")).contains("WMS");
    }

    @Test
    void opensEndpointCircuitAfterFailures() {
        IntegrationApplicationServiceTest.MemoryIntegrationMapper mapper =
                new IntegrationApplicationServiceTest.MemoryIntegrationMapper();
        mapper.insertEndpoint(new IntegrationMapper.EndpointRow(null, "IE1", "WMS", "HTTP",
                "http://127.0.0.1:1/events", 100, 1, 0, 1, 1));
        ConfiguredIntegrationTransportAdapter adapter = new ConfiguredIntegrationTransportAdapter(mapper);

        IntegrationTransportPort.DeliveryResult first = adapter.deliver(
                new IntegrationTransportPort.DeliveryRequest("IM1", "PurchaseOrderReleased", "PURCHASE",
                        "WMS", "HTTP", "PO-1", "idem-1", "{}"));
        IntegrationTransportPort.DeliveryResult second = adapter.deliver(
                new IntegrationTransportPort.DeliveryRequest("IM2", "PurchaseOrderReleased", "PURCHASE",
                        "WMS", "HTTP", "PO-2", "idem-2", "{}"));

        assertThat(first.success()).isFalse();
        assertThat(second.failureReason()).isEqualTo("enabled endpoint not configured");
        assertThat(mapper.findEndpoint("IE1").status()).isEqualTo(2);
    }
}
