package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.iam.application.IamApplicationService;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class IamController {
    private final IamApplicationService service;

    public IamController(IamApplicationService service) {
        this.service = service;
    }

    @PostMapping("/api/iam/v1/auth/login")
    public ApiResponse<IamApplicationService.LoginResult> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        return ok(service.login(body.username(), body.password()), request);
    }

    @PostMapping("/api/iam/v1/auth/refresh")
    public ApiResponse<IamApplicationService.LoginResult> refresh(@Valid @RequestBody RefreshRequest body, HttpServletRequest request) {
        return ok(service.refresh(body.refreshToken()), request);
    }

    @PostMapping("/api/iam/v1/auth/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest body, HttpServletRequest request) {
        service.logout(body.refreshToken());
        return ok(null, request);
    }

    @GetMapping("/api/iam/v1/me")
    public ApiResponse<IamApplicationService.UserView> me(@RequestHeader("Authorization") String token, HttpServletRequest request) {
        return ok(service.me(token.replace("Bearer ", "")), request);
    }

    @PostMapping("/api/iam/v1/users")
    public ApiResponse<IamApplicationService.UserView> createUser(@Valid @RequestBody UserRequest body, HttpServletRequest request) {
        return ok(service.createUser(body.username(), body.password()), request);
    }

    @GetMapping("/api/iam/v1/users")
    public ApiResponse<List<IamMapper.UserRow>> users(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.users(limit), request);
    }

    @PostMapping("/api/iam/v1/roles")
    public ApiResponse<IamApplicationService.RoleView> createRole(@Valid @RequestBody RoleRequest body, HttpServletRequest request) {
        return ok(service.createRole(body.code(), body.name()), request);
    }

    @GetMapping("/api/iam/v1/roles")
    public ApiResponse<List<IamMapper.RoleRow>> roles(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.roles(limit), request);
    }

    @PostMapping("/api/iam/v1/roles/bind-user")
    public ApiResponse<Void> bindUserRole(@Valid @RequestBody BindUserRoleRequest body, HttpServletRequest request) {
        service.bindUserRole(body.userId(), body.roleId());
        return ok(null, request);
    }

    @PostMapping("/api/iam/v1/roles/grant-permission")
    public ApiResponse<Void> grantRolePermission(@Valid @RequestBody GrantPermissionRequest body, HttpServletRequest request) {
        service.grantRolePermission(body.roleId(), body.permissionCode());
        return ok(null, request);
    }

    @PostMapping("/api/iam/v1/permissions")
    public ApiResponse<Void> createPermission(@Valid @RequestBody PermissionRequest body, HttpServletRequest request) {
        service.createPermission(body.appCode(), body.code(), body.name());
        return ok(null, request);
    }

    @GetMapping("/api/iam/v1/permissions")
    public ApiResponse<List<IamMapper.PermissionRow>> permissions(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.permissions(limit), request);
    }

    @PostMapping("/api/iam/v1/data-scopes")
    public ApiResponse<Void> createDataScope(@Valid @RequestBody DataScopeRequest body, HttpServletRequest request) {
        service.createDataScope(body.roleId(), body.type(), body.value());
        return ok(null, request);
    }

    @GetMapping("/openapi/iam/v1/data-scope")
    public ApiResponse<List<IamMapper.DataScopeRow>> dataScopes(@RequestParam long roleId, HttpServletRequest request) {
        return ok(service.dataScopes(roleId), request);
    }

    @PostMapping("/api/iam/v1/approval-instances")
    public ApiResponse<IamApplicationService.ApprovalView> createApproval(@Valid @RequestBody ApprovalRequest body, HttpServletRequest request) {
        return ok(service.createApproval(body.businessType(), body.businessNo()), request);
    }

    @PostMapping("/api/iam/v1/approval-instances/{approvalNo}/complete")
    public ApiResponse<Void> completeApproval(@PathVariable String approvalNo, @Valid @RequestBody CompleteApprovalRequest body, HttpServletRequest request) {
        service.completeApproval(approvalNo, body.approved(), body.version());
        return ok(null, request);
    }

    @GetMapping("/api/iam/v1/approval-instances")
    public ApiResponse<List<IamMapper.ApprovalRow>> approvals(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.approvals(limit), request);
    }

    @GetMapping("/api/iam/v1/operation-logs")
    public ApiResponse<List<IamMapper.OperationLogRow>> operationLogs(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.operationLogs(limit), request);
    }

    @PostMapping("/api/iam/v1/security-policies")
    public ApiResponse<Void> createSecurityPolicy(@Valid @RequestBody SecurityPolicyRequest body, HttpServletRequest request) {
        service.createSecurityPolicy(body.code(), body.value());
        return ok(null, request);
    }

    @GetMapping("/api/iam/v1/security-policies")
    public ApiResponse<List<IamMapper.SecurityPolicyRow>> securityPolicies(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.securityPolicies(limit), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record UserRequest(@NotBlank String username, @NotBlank String password) {}
    public record RoleRequest(@NotBlank String code, @NotBlank String name) {}
    public record BindUserRoleRequest(@Positive long userId, @Positive long roleId) {}
    public record GrantPermissionRequest(@Positive long roleId, @NotBlank String permissionCode) {}
    public record PermissionRequest(@NotBlank String appCode, @NotBlank String code, @NotBlank String name) {}
    public record DataScopeRequest(@Positive long roleId, @NotBlank String type, @NotBlank String value) {}
    public record ApprovalRequest(@NotBlank String businessType, @NotBlank String businessNo) {}
    public record CompleteApprovalRequest(boolean approved, @PositiveOrZero int version) {}
    public record SecurityPolicyRequest(@NotBlank String code, @NotBlank String value) {}
}
