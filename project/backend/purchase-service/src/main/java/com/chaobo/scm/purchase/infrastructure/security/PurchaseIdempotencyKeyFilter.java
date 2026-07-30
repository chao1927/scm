package com.chaobo.scm.purchase.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.io.IOException;
import java.util.Set;

/**
 * PurchaseIdempotencyKeyFilter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class PurchaseIdempotencyKeyFilter extends OncePerRequestFilter {

    /**
     * SAFE_METHODS（类型：{@code Set<String>}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    /**
     * 处理当前类型职责中的操作 {@code doFilterInternal}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param response 处理结果，类型为 {@code HttpServletResponse}
     * @param chain 业务处理参数或成员，类型为 {@code FilterChain}
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!SAFE_METHODS.contains(request.getMethod())) {
            String key = request.getHeader("X-Idempotency-Key");
            if (key == null || key.isBlank() || key.length() > DO_FILTER_INTERNAL_VALUE_128) {
                response.sendError(400, "X-Idempotency-Key is required and must not exceed 128 characters");
                return;
            }
        }
        if (SAFE_METHODS.contains(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new ContentCachingRequestWrapper(request, 1024 * 1024), response);
    }

    /**
     * 业务常量 {@code DO_FILTER_INTERNAL_VALUE_128}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int DO_FILTER_INTERNAL_VALUE_128 = 128;
}
