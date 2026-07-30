package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.oms.application.OmsExternalEvent;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HTTP 事件入口仅允许受控人工补偿的契约测试。
 */
class OmsInternalEventControllerTest {

    @Test
    void requiresManualPermissionAndReasonBeforeReusingInboxHandler() {
        AtomicReference<OmsExternalEvent> handled = new AtomicReference<>();
        var controller = new OmsInternalEventController(handled::set);
        var request = request();

        assertThatThrownBy(() -> controller.manualConsume(
                request, "故障恢复", authentication(Set.of("oms:event:consume"))))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code())
                                .isEqualTo(ErrorCode.FORBIDDEN));
        assertThatThrownBy(() -> controller.manualConsume(
                request, " ", authentication(Set.of("oms:event:manual-consume"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("原因");

        controller.manualConsume(request, " RocketMQ 故障恢复 ",
                authentication(Set.of("oms:event:manual-consume")));
        assertThat(handled.get().eventCode()).isEqualTo("INV-E1");
    }

    private static OmsInternalEventController.EventRequest request() {
        return new OmsInternalEventController.EventRequest(
                "INVENTORY", "INV-E1", "StockReserved", "SO-1",
                "FUL-1", "REF-1", "INV-1", new BigDecimal("2"),
                null, null, null, null, null, null, null, false, "{}");
    }

    private static TestingAuthenticationToken authentication(
            Set<String> permissions) {
        var token = new TestingAuthenticationToken("tester", "secret");
        token.setDetails(new ScmAccessContext(
                1, "tester", "OMS", permissions,
                Map.of("ORGANIZATION", Set.of("*"), "OWNER", Set.of("*"),
                        "WAREHOUSE", Set.of("*"))));
        return token;
    }
}
