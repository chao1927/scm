package com.chaobo.scm.purchase.infrastructure.integration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 采购目标上下文路由配置契约测试。
 *
 * <p>确保七类主动命令均有独立环境变量入口，且 URL 默认留空以执行失败关闭。
 */
class PurchaseIntegrationRouteConfigurationTest {

    @Test
    void applicationConfigurationDeclaresEveryRouteAndTimeout() throws IOException {
        String yaml;
        try (var input = getClass().getClassLoader()
                .getResourceAsStream("application.yml")) {
            assertThat(input).isNotNull();
            yaml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        Map<String, String> routes = Map.of(
                "supplier-create-po-confirm-todo",
                "PURCHASE_ROUTE_SUPPLIER_PO_CONFIRM_URL",
                "wms-create-purchase-inbound-plan",
                "PURCHASE_ROUTE_WMS_INBOUND_URL",
                "bms-create-purchase-payable-plan",
                "PURCHASE_ROUTE_BMS_PAYABLE_URL",
                "inventory-lock-supplier-return",
                "PURCHASE_ROUTE_INVENTORY_RETURN_LOCK_URL",
                "wms-create-supplier-return-outbound",
                "PURCHASE_ROUTE_WMS_RETURN_OUTBOUND_URL",
                "tms-create-supplier-return-transport",
                "PURCHASE_ROUTE_TMS_RETURN_TRANSPORT_URL",
                "bms-create-supplier-return-offset",
                "PURCHASE_ROUTE_BMS_RETURN_OFFSET_URL"
        );
        routes.forEach((route, variable) -> assertThat(yaml)
                .contains(route + ":")
                .contains("${" + variable + ":}"));
        assertThat(yaml)
                .contains("connect-timeout-ms: 1000")
                .contains("read-timeout-ms: 3000")
                .contains("failure-threshold: 5")
                .contains("circuit-open-ms: 30000");
    }
}
