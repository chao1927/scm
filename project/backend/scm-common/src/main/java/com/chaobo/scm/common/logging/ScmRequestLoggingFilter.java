package com.chaobo.scm.common.logging;

import com.chaobo.scm.common.security.ScmAccessContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 统一记录 HTTP 边界的操作审计日志。
 *
 * <p>过滤器只记录经过白名单筛选的元数据，不记录请求体、查询参数、Cookie、令牌或口令。每次请求均产生
 * requestId/traceId，并在结束时记录操作人、操作、结果、状态码和耗时；未处理异常同时保留堆栈用于定位。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class ScmRequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final Logger LOG = LoggerFactory.getLogger(ScmRequestLoggingFilter.class);
    private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    private static final String SYSTEM_OPERATOR = "system";

    private final String applicationName;

    /**
     * 创建请求日志过滤器。
     *
     * @param applicationName 当前服务名
     */
    public ScmRequestLoggingFilter(String applicationName) {
        this.applicationName = normalize(applicationName, "unknown-service");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        String requestId = correlationId(request.getHeader(REQUEST_ID_HEADER));
        String traceId = correlationId(request.getHeader(TRACE_ID_HEADER));
        response.setHeader(REQUEST_ID_HEADER, requestId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        putContext(requestId, traceId);
        try {
            addOperatorContext();
            filterChain.doFilter(request, response);
            addOperatorContext();
            writeCompletion(request, response.getStatus(), elapsedMillis(startedAt));
        } catch (ServletException | IOException | RuntimeException exception) {
            addOperatorContext();
            LOG.error("event=http_request_failed service={} operation={} result=FAILURE status={} durationMs={} exceptionType={}",
                    applicationName, operation(request), response.getStatus(), elapsedMillis(startedAt),
                    exception.getClass().getSimpleName(), exception);
            throw exception;
        } finally {
            clearContext();
        }
    }

    private void writeCompletion(HttpServletRequest request, int status, long durationMillis) {
        String result = status >= 500 ? "FAILURE" : status >= 400 ? "REJECTED" : "SUCCESS";
        String message = "event=http_request_completed service={} operation={} result={} status={} durationMs={}";
        if (status >= 500) {
            LOG.error(message, applicationName, operation(request), result, status, durationMillis);
        } else {
            LOG.info(message, applicationName, operation(request), result, status, durationMillis);
        }
    }

    private static void putContext(String requestId, String traceId) {
        MDC.put("requestId", requestId);
        MDC.put("traceId", traceId);
        MDC.put("operatorId", "0");
        MDC.put("operator", SYSTEM_OPERATOR);
    }

    private static void addOperatorContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof ScmAccessContext context) {
            MDC.put("operatorId", Long.toString(context.operatorId()));
            MDC.put("operator", normalize(context.username(), SYSTEM_OPERATOR));
        }
    }

    private static String correlationId(String candidate) {
        if (candidate != null && SAFE_CORRELATION_ID.matcher(candidate).matches()) {
            return candidate;
        }
        return UUID.randomUUID().toString();
    }

    private static String operation(HttpServletRequest request) {
        return normalize(request.getMethod(), "UNKNOWN") + " " + normalize(request.getRequestURI(), "/");
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }

    private static String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.replace('\n', '_').replace('\r', '_');
    }

    private static void clearContext() {
        MDC.remove("requestId");
        MDC.remove("traceId");
        MDC.remove("operatorId");
        MDC.remove("operator");
    }
}
