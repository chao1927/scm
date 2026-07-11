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

@Component
public class CommandContextFactory {
    public CommandContext create(HttpServletRequest request, Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "当前请求没有有效访问令牌");
        }
        var jwt = jwtAuthentication.getToken();
        long operatorId = Long.parseLong(jwt.getSubject());
        Number supplierClaim = jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null;
        Long supplierId = supplierClaim == null ? null : supplierClaim.longValue();
        long organizationId = headerAsLong(request, "X-Org-Id", 0L);
        Set<String> permissions = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority()).collect(Collectors.toUnmodifiableSet());
        return new CommandContext(operatorId, jwt.getClaimAsString("name"), organizationId,
                supplierId, header(request, "X-Request-Id"), header(request, "X-Trace-Id"),
                header(request, "X-Idempotency-Key"), permissions);
    }

    private String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null ? "" : value;
    }

    private long headerAsLong(HttpServletRequest request, String name, long defaultValue) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? defaultValue : Long.parseLong(value);
    }
}
