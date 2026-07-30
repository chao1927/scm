package com.chaobo.scm.supplier.infrastructure.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "当前请求没有有效访问令牌");
        }
        var jwt = jwtAuthentication.getToken();
        long operatorId = Long.parseLong(jwt.getSubject());
        Number supplierClaim = jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null;
        Long supplierId = supplierClaim == null ? null : supplierClaim.longValue();
        long organizationId = headerAsLong(request, "X-Org-Id", 0L);
        Set<String> permissions = authentication.getAuthorities().stream().map(authority -> authority.getAuthority()).collect(Collectors.toUnmodifiableSet());
        return new CommandContext(operatorId, jwt.getClaimAsString("name"), organizationId, supplierId, header(request, "X-Request-Id"), header(request, "X-Trace-Id"), header(request, "X-Idempotency-Key"), permissions);
    }

    /**
     * 处理当前类型职责中的操作 {@code header}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null ? "" : value;
    }

    /**
     * 处理当前类型职责中的操作 {@code headerAsLong}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param defaultValue 业务处理参数或成员，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private long headerAsLong(HttpServletRequest request, String name, long defaultValue) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? defaultValue : Long.parseLong(value);
    }
}
