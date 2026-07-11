package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.profile.*;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.domain.profile.ProfileFieldChange;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier/v1/profile")
public class SupplierProfileController {
    private final ProfileApplicationService service; private final CommandContextFactory contexts;
    public SupplierProfileController(ProfileApplicationService service, CommandContextFactory contexts) { this.service = service; this.contexts = contexts; }

    @GetMapping @PreAuthorize("hasAuthority('supplier:profile:read')")
    public ApiResponse<ProfileViews.Profile> profile(@RequestParam long supplierId,
                                                     @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ApiResponse.success(service.profile(supplierId, scope(jwt)), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }
    @GetMapping("/change-requests") @PreAuthorize("hasAuthority('supplier:profile:read')")
    public ApiResponse<PageResult<ProfileViews.Change>> changes(@RequestParam long supplierId,
            @RequestParam(required=false) Integer status, @RequestParam(defaultValue="1") int pageNo,
            @RequestParam(defaultValue="20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ApiResponse.success(service.changes(supplierId, scope(jwt), status, pageNo, pageSize), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }
    @PostMapping("/change-requests")
    public ApiResponse<CommandResult> submit(@Valid @RequestBody SubmitRequest body, HttpServletRequest request, Authentication auth) {
        var changes = body.changedFields().stream().map(v -> new ProfileFieldChange(v.fieldCode(), v.beforeValue(), v.afterValue())).toList();
        return ApiResponse.success(service.submit(body.supplierId(), body.profileVersion(), body.changeReason(), changes,
                contexts.create(request, auth)), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }
    @PostMapping("/change-requests/{id}/withdraw")
    public ApiResponse<CommandResult> withdraw(@PathVariable long id, @Valid @RequestBody WithdrawRequest body,
                                                HttpServletRequest request, Authentication auth) {
        return ApiResponse.success(service.withdraw(id, body.version(), body.reason(), contexts.create(request, auth)),
                request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }
    private Long scope(Jwt jwt) { Number n = jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null; return n == null ? null : n.longValue(); }
    public record SubmitRequest(@Positive long supplierId, @PositiveOrZero int profileVersion,
                                @NotBlank String changeReason, @NotEmpty List<@Valid FieldChangeRequest> changedFields) {}
    public record FieldChangeRequest(@NotBlank String fieldCode, String beforeValue, String afterValue) {}
    public record WithdrawRequest(@PositiveOrZero int version, @NotBlank String reason) {}
}
