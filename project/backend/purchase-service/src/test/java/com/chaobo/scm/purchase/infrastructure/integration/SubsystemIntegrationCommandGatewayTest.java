package com.chaobo.scm.purchase.infrastructure.integration;

import com.chaobo.scm.purchase.infrastructure.persistence.integration.IntegrationCommandMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

/**
 * 采购目标上下文 HTTP 路由契约测试。
 *
 * <p>使用 Spring 模拟传输验证真实 URL、请求头、请求体、超时和熔断行为，不依赖目标子系统。
 */
class SubsystemIntegrationCommandGatewayTest {

    private static final Map<String, String> TARGETS = Map.of(
            "SUPPLIER_CREATE_PO_CONFIRM_TODO", "SUPPLIER",
            "WMS_CREATE_PURCHASE_INBOUND_PLAN", "WMS",
            "BMS_CREATE_PURCHASE_PAYABLE_PLAN", "BMS",
            "INVENTORY_LOCK_SUPPLIER_RETURN", "INVENTORY",
            "WMS_CREATE_SUPPLIER_RETURN_OUTBOUND", "WMS",
            "TMS_CREATE_SUPPLIER_RETURN_TRANSPORT", "TMS",
            "BMS_CREATE_SUPPLIER_RETURN_OFFSET", "BMS"
    );

    @Test
    void everyPurchaseCommandUsesItsConfiguredUrlAndRequiredHeaders() {
        var fixture = fixture("service-token", 5);
        int index = 0;
        long commandId = 100L;
        for (var route : TARGETS.entrySet()) {
            String url = "https://targets.local/commands/" + index++;
            fixture.environment().setProperty(property(route.getKey()), url);
            fixture.server()
                    .expect(requestTo(url))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header("Authorization", "Bearer service-token"))
                    .andExpect(header("X-Idempotency-Key",
                            Long.toString(commandId)))
                    .andExpect(header("X-Source-System", "PURCHASE"))
                    .andExpect(header("X-Target-System", route.getValue()))
                    .andExpect(header("X-Command-Type", route.getKey()))
                    .andExpect(header("X-Business-Type", "PURCHASE_ORDER"))
                    .andExpect(header("X-Business-Id", "9001"))
                    .andExpect(header("X-Business-No", "PO-9001"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json("{\"version\":1}"))
                    .andRespond(withNoContent());
            commandId++;
        }

        commandId = 100L;
        for (var route : TARGETS.entrySet()) {
            fixture.gateway().dispatch(
                    command(commandId++, route.getKey(), route.getValue()));
        }
        fixture.server().verify();
    }

    @Test
    void repeatedDeliveryKeepsTheSameTargetIdempotencyKey() {
        var fixture = fixture("service-token", 5);
        String commandType = "WMS_CREATE_PURCHASE_INBOUND_PLAN";
        String url = "https://wms.local/purchase-inbounds";
        fixture.environment().setProperty(property(commandType), url);
        fixture.server()
                .expect(ExpectedCount.twice(), requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Idempotency-Key", "8801"))
                .andExpect(header("X-Command-Type", commandType))
                .andRespond(withNoContent());
        var command = command(8801L, commandType, "WMS");

        var first = fixture.gateway().dispatch(command);
        var repeated = fixture.gateway().dispatch(command);

        assertThat(first.remoteReference())
                .isEqualTo(repeated.remoteReference())
                .isEqualTo("WMS:8801");
        fixture.server().verify();
    }

    @Test
    void clientServerAndTimeoutFailuresAreReportedToDispatcher() {
        var fixture = fixture("service-token", 10);
        fixture.environment()
                .withProperty(
                        property("SUPPLIER_CREATE_PO_CONFIRM_TODO"),
                        "https://supplier.local/client-error")
                .withProperty(
                        property("BMS_CREATE_PURCHASE_PAYABLE_PLAN"),
                        "https://bms.local/server-error")
                .withProperty(
                        property("WMS_CREATE_PURCHASE_INBOUND_PLAN"),
                        "https://wms.local/timeout");
        fixture.server()
                .expect(requestTo("https://supplier.local/client-error"))
                .andRespond(withBadRequest());
        fixture.server()
                .expect(requestTo("https://bms.local/server-error"))
                .andRespond(withServerError());
        fixture.server()
                .expect(requestTo("https://wms.local/timeout"))
                .andRespond(request -> {
                    throw new ResourceAccessException(
                            "I/O error on POST request: Read timed out");
                });

        assertThatThrownBy(() -> fixture.gateway().dispatch(command(
                1L, "SUPPLIER_CREATE_PO_CONFIRM_TODO", "SUPPLIER")))
                .isInstanceOf(RestClientResponseException.class)
                .extracting(exception -> ((RestClientResponseException) exception)
                        .getStatusCode().value())
                .isEqualTo(400);
        assertThatThrownBy(() -> fixture.gateway().dispatch(command(
                2L, "BMS_CREATE_PURCHASE_PAYABLE_PLAN", "BMS")))
                .isInstanceOf(RestClientResponseException.class)
                .extracting(exception -> ((RestClientResponseException) exception)
                        .getStatusCode().value())
                .isEqualTo(500);
        assertThatThrownBy(() -> fixture.gateway().dispatch(command(
                3L, "WMS_CREATE_PURCHASE_INBOUND_PLAN", "WMS")))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("Read timed out");
        fixture.server().verify();
    }

    @Test
    void consecutiveRemoteFailuresOpenOnlyTheTargetCircuit() {
        var fixture = fixture("service-token", 2);
        fixture.environment()
                .withProperty(
                        property("WMS_CREATE_PURCHASE_INBOUND_PLAN"),
                        "https://wms.local/failure")
                .withProperty(
                        property("BMS_CREATE_PURCHASE_PAYABLE_PLAN"),
                        "https://bms.local/success");
        fixture.server()
                .expect(ExpectedCount.twice(),
                        requestTo("https://wms.local/failure"))
                .andRespond(withServerError());
        fixture.server()
                .expect(requestTo("https://bms.local/success"))
                .andRespond(withNoContent());
        var wms = command(
                1L, "WMS_CREATE_PURCHASE_INBOUND_PLAN", "WMS");

        assertThatThrownBy(() -> fixture.gateway().dispatch(wms))
                .isInstanceOf(RestClientResponseException.class);
        assertThatThrownBy(() -> fixture.gateway().dispatch(wms))
                .isInstanceOf(RestClientResponseException.class);
        assertThatThrownBy(() -> fixture.gateway().dispatch(wms))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("熔断");
        assertThat(fixture.gateway().dispatch(command(
                2L, "BMS_CREATE_PURCHASE_PAYABLE_PLAN", "BMS"))
                .remoteReference()).isEqualTo("BMS:2");
        fixture.server().verify();
    }

    @Test
    void missingUnknownAndTargetMismatchRoutesAlwaysFailClosed() {
        var missing = fixture("service-token", 1);
        var wmsCommand = command(
                1L, "WMS_CREATE_PURCHASE_INBOUND_PLAN", "WMS");
        assertThatThrownBy(() -> missing.gateway().dispatch(wmsCommand))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未配置跨子系统命令路由");
        assertThatThrownBy(() -> missing.gateway().dispatch(wmsCommand))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未配置跨子系统命令路由");

        var unknown = fixture("service-token", 1);
        unknown.environment().withProperty(
                "scm.integration.routes.unknown-command.url",
                "https://unknown.local/should-not-call");
        assertThatThrownBy(() -> unknown.gateway().dispatch(
                command(2L, "UNKNOWN_COMMAND", "WMS")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("不支持的采购集成命令");

        var mismatch = fixture("service-token", 1);
        mismatch.environment().withProperty(
                property("SUPPLIER_CREATE_PO_CONFIRM_TODO"),
                "https://supplier.local/should-not-call");
        assertThatThrownBy(() -> mismatch.gateway().dispatch(command(
                3L, "SUPPLIER_CREATE_PO_CONFIRM_TODO", "WMS")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("目标子系统不匹配");

        var missingToken = fixture("", 1);
        missingToken.environment().withProperty(
                property("WMS_CREATE_PURCHASE_INBOUND_PLAN"),
                "https://wms.local/should-not-call");
        assertThatThrownBy(() -> missingToken.gateway().dispatch(wmsCommand))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未配置跨子系统访问令牌");
    }

    private static Fixture fixture(String token, int failureThreshold) {
        var environment = new MockEnvironment();
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer
                .bindTo(builder)
                .ignoreExpectOrder(true)
                .build();
        var gateway = new SubsystemIntegrationCommandGateway(
                environment,
                token,
                builder.build(),
                failureThreshold,
                30_000L
        );
        return new Fixture(environment, gateway, server);
    }

    private static String property(String commandType) {
        return "scm.integration.routes."
                + commandType.toLowerCase(Locale.ROOT).replace('_', '-')
                + ".url";
    }

    private static IntegrationCommandMapper.CommandRow command(
            long commandId,
            String commandType,
            String targetSystem
    ) {
        return new IntegrationCommandMapper.CommandRow(
                commandId,
                commandType,
                targetSystem,
                "PURCHASE_ORDER",
                "9001",
                "PO-9001",
                "{\"version\":1}",
                2,
                0
        );
    }

    private record Fixture(
            MockEnvironment environment,
            SubsystemIntegrationCommandGateway gateway,
            MockRestServiceServer server
    ) {
    }
}
