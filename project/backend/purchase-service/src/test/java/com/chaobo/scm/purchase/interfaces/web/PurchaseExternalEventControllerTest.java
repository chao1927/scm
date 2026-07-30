package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.purchase.application.integration.PurchaseExternalEvent;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 外部事件 HTTP 人工补偿入口权限契约测试。
 */
class PurchaseExternalEventControllerTest {

    @Test
    void shouldRejectRequestWithoutManualOperationPermission() {
        var controller = new PurchaseExternalEventController(event -> {
        });
        var request = request();
        var authentication = authentication(Set.of("purchase:read"));

        assertThrows(BusinessException.class, () -> controller.manualConsume(
                body(), request, authentication
        ));
    }

    @Test
    void shouldRejectRequestWithoutManualOperationReason() {
        var controller = new PurchaseExternalEventController(event -> {
        });
        var request = request();
        var authentication = authentication(Set.of("purchase:event:manual-consume"));

        assertThrows(BusinessException.class, () -> controller.manualConsume(
                body(), request, authentication
        ));
    }

    @Test
    void shouldAllowAuditedManualCompatibilityConsumption() {
        var consumed = new ArrayList<PurchaseExternalEvent>();
        var controller = new PurchaseExternalEventController(consumed::add);
        var request = request();
        request.addHeader("X-Manual-Operation-Reason", "补偿历史失败事件");
        var authentication = authentication(Set.of("purchase:event:manual-consume"));

        controller.manualConsume(body(), request, authentication);

        assertEquals(1, consumed.size());
        assertEquals("EVT-HTTP-001", consumed.get(0).eventCode());
    }

    private static PurchaseExternalEventController.Request body() {
        return new PurchaseExternalEventController.Request(
                "WmsReceiptCompleted",
                null, null, null, null, "IN-001", null,
                null, null,
                null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null,
                Map.of()
        );
    }

    private static MockHttpServletRequest request() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Source-System", "WMS");
        request.addHeader("X-Event-Code", "EVT-HTTP-001");
        return request;
    }

    private static TestingAuthenticationToken authentication(Set<String> permissions) {
        var authentication = new TestingAuthenticationToken("operator", "password");
        authentication.setDetails(new ScmAccessContext(
                1L, "operator", "PURCHASE", permissions, Map.of()
        ));
        return authentication;
    }
}
