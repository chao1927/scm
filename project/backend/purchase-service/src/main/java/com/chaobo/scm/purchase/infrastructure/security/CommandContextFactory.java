package com.chaobo.scm.purchase.infrastructure.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * CommandContextFactory。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class CommandContextFactory {

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code CommandContext}
     */
    public CommandContext create(HttpServletRequest request, Authentication authentication) {
        ScmAccessContext access = accessContext(authentication);
        var tenantId = scopedLong(request, access, "X-Tenant-Id", "TENANT", 0L);
        var purchaseOrgScope = optionalLong(request.getHeader("X-Purchase-Org-Id"));
        if (purchaseOrgScope != null) {
            access.requireScope("PURCHASE_ORG", String.valueOf(purchaseOrgScope));
        }
        return new CommandContext(access.operatorId(), access.username(), tenantId, purchaseOrgScope, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"), request.getHeader("X-Idempotency-Key"), access.permissions(), requestDigest(request));
    }

    /**
     * 处理当前类型职责中的操作 {@code accessContext}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ScmAccessContext}
     */
    private static ScmAccessContext accessContext(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof ScmAccessContext access)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "当前请求没有有效访问令牌");
        }
        return access;
    }

    /**
     * 处理当前类型职责中的操作 {@code scopedLong}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param access 业务处理参数或成员，类型为 {@code ScmAccessContext}
     * @param header 业务处理参数或成员，类型为 {@code String}
     * @param scopeType 业务处理参数或成员，类型为 {@code String}
     * @param fallback 业务处理参数或成员，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private static long scopedLong(HttpServletRequest request, ScmAccessContext access, String header, String scopeType, long fallback) {
        String value = request.getHeader(header);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        access.requireScope(scopeType, value);
        return Long.parseLong(value);
    }

    /**
     * 处理当前类型职责中的操作 {@code optionalLong}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private static Long optionalLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestDigest}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String requestDigest(HttpServletRequest request) {
        byte[] body = request instanceof ContentCachingRequestWrapper wrapper ? wrapper.getContentAsByteArray() : new byte[0];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(request.getMethod().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) '\n');
            digest.update(request.getRequestURI().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) '\n');
            digest.update(body);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
