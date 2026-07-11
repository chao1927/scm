package com.chaobo.scm.purchase.infrastructure.security;

import com.chaobo.scm.purchase.application.shared.CommandContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CommandContextFactory {

    public CommandContext create(HttpServletRequest request, Authentication authentication) {
        var operatorId = parseLong(request.getHeader("X-Operator-Id"), 0L);
        var operatorName = request.getHeader("X-Operator-Name");
        var tenantId = parseLong(request.getHeader("X-Tenant-Id"), 0L);
        var purchaseOrgScope = optionalLong(request.getHeader("X-Purchase-Org-Id"));
        var permissions = permissions(authentication);
        return new CommandContext(
                operatorId,
                operatorName == null ? "系统用户" : operatorName,
                tenantId,
                purchaseOrgScope,
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"),
                request.getHeader("X-Idempotency-Key"),
                permissions);
    }

    private static Set<String> permissions(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return Set.of(
                    "purchase:requisition:create",
                    "purchase:requisition:update",
                    "purchase:requisition:submit",
                    "purchase:requisition:approve",
                    "purchase:requisition:convert",
                    "purchase:rfq:create",
                    "purchase:rfq:publish",
                    "purchase:rfq:close",
                    "purchase:rfq:read",
                    "purchase:comparison:generate",
                    "purchase:comparison:award",
                    "purchase:comparison:read",
                    "purchase:price:create",
                    "purchase:price:disable",
                    "purchase:price:read",
                    "purchase:po:create",
                    "purchase:po:submit",
                    "purchase:po:approve",
                    "purchase:po:publish",
                    "purchase:po:cancel",
                    "purchase:po:read",
                    "purchase:po-change:create",
                    "purchase:po-change:approve",
                    "purchase:po-change:read",
                    "purchase:inbound:record-asn",
                    "purchase:inbound:sync-wms",
                    "purchase:inbound:read",
                    "purchase:supplier-return:create",
                    "purchase:supplier-return:submit",
                    "purchase:supplier-return:approve",
                    "purchase:supplier-return:notify",
                    "purchase:supplier-return:read");
        }
        var values = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(value -> value.startsWith("SCOPE_") ? value.substring(6) : value)
                .collect(Collectors.toSet());
        if (values.isEmpty()) {
            return Set.of();
        }
        return values;
    }

    private static Long optionalLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    private static long parseLong(String value, long fallback) {
        return value == null || value.isBlank() ? fallback : Long.parseLong(value);
    }
}
