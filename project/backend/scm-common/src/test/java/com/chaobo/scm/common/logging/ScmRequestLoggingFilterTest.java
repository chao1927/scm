package com.chaobo.scm.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * {@link ScmRequestLoggingFilter} 的边界与脱敏契约测试。
 *
 * @author SCM Team
 */
class ScmRequestLoggingFilterTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        logger = (Logger) LoggerFactory.getLogger(ScmRequestLoggingFilter.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
        MDC.clear();
    }

    @Test
    void shouldRecordSuccessfulOperationAndReturnCorrelationHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/purchase-orders");
        request.addHeader(ScmRequestLoggingFilter.REQUEST_ID_HEADER, "request-1001");
        request.addHeader(ScmRequestLoggingFilter.TRACE_ID_HEADER, "trace-1001");
        request.setQueryString("password=never-log-this");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ScmRequestLoggingFilter("purchase-service").doFilter(request, response,
                (currentRequest, currentResponse) -> ((MockHttpServletResponse) currentResponse).setStatus(201));

        assertThat(response.getHeader(ScmRequestLoggingFilter.REQUEST_ID_HEADER)).isEqualTo("request-1001");
        assertThat(response.getHeader(ScmRequestLoggingFilter.TRACE_ID_HEADER)).isEqualTo("trace-1001");
        ILoggingEvent event = appender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage())
                .contains("event=http_request_completed", "operation=POST /api/purchase-orders",
                        "result=SUCCESS", "status=201")
                .doesNotContain("password", "never-log-this");
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    void shouldReplaceUnsafeCorrelationIdAndRecordUnhandledException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.addHeader(ScmRequestLoggingFilter.REQUEST_ID_HEADER, "bad\nforged-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ScmRequestLoggingFilter filter = new ScmRequestLoggingFilter("oms-service");

        assertThatThrownBy(() -> filter.doFilter(request, response,
                (currentRequest, currentResponse) -> {
                    throw new ServletException("database unavailable");
                })).isInstanceOf(ServletException.class);

        assertThat(response.getHeader(ScmRequestLoggingFilter.REQUEST_ID_HEADER))
                .matches("[0-9a-f-]{36}")
                .isNotEqualTo("bad\nforged-id");
        List<ILoggingEvent> errors = appender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR).toList();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getFormattedMessage())
                .contains("event=http_request_failed", "result=FAILURE", "exceptionType=ServletException");
        assertThat(errors.get(0).getThrowableProxy()).isNotNull();
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
}
