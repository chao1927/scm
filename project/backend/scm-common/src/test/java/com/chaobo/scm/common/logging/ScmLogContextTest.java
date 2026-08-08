package com.chaobo.scm.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * {@link ScmLogContext} 的线程复用安全测试。
 *
 * @author SCM Team
 */
class ScmLogContextTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldInstallSystemContextAndRestorePreviousValues() {
        MDC.put("requestId", "parent-request");
        MDC.put("traceId", "parent-trace");

        try (ScmLogContext ignored = ScmLogContext.openSystem("message-1001")) {
            assertThat(MDC.get("requestId")).isEqualTo("message-1001");
            assertThat(MDC.get("traceId")).isEqualTo("message-1001");
            assertThat(MDC.get("operatorId")).isEqualTo("0");
            assertThat(MDC.get("operator")).isEqualTo("system");
        }

        assertThat(MDC.get("requestId")).isEqualTo("parent-request");
        assertThat(MDC.get("traceId")).isEqualTo("parent-trace");
        assertThat(MDC.get("operatorId")).isNull();
        assertThat(MDC.get("operator")).isNull();
    }

    @Test
    void shouldReplaceUnsafeReferenceAndLeaveEmptyThreadClean() {
        try (ScmLogContext ignored = ScmLogContext.openSystem("forged\nreference")) {
            assertThat(MDC.get("requestId"))
                    .matches("[0-9a-f-]{36}")
                    .doesNotContain("forged");
        }

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
        assertThat(ScmLogContext.reference(null)).matches("[0-9a-f-]{36}");
    }
}
