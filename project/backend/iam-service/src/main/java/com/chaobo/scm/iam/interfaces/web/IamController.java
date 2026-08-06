package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.iam.application.IamApplicationService;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * IamController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping
public class IamController {

    /**
     * service（类型：{@code IamApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamApplicationService service;

    /**
     * 创建 IamController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code IamApplicationService}
     */
    public IamController(IamApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code login}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code LoginRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<IamApplicationService.LoginResult>}
     */
    @PostMapping("/api/iam/v1/auth/login")
    public ApiResponse<IamApplicationService.LoginResult> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        String appCode = body.appCode() == null || body.appCode().isBlank()
            ? "SCM_WEB" : body.appCode();
        String deviceDigest = body.deviceDigest() == null || body.deviceDigest().isBlank()
            ? deviceDigest(request) : body.deviceDigest();
        return ok(service.login(new IamApplicationService.LoginCommand(
            body.username(), body.password(), appCode, deviceDigest)), request);
    }

    /** 仅允许使用已验证且与设备/登录会话绑定的 challengeId 签发 Token。 */
    @PostMapping("/api/iam/v1/auth/mfa/complete")
    public ApiResponse<IamApplicationService.LoginResult> completeMfaLogin(
            @Valid @RequestBody MfaLoginRequest body, HttpServletRequest request) {
        String deviceDigest = body.deviceDigest() == null || body.deviceDigest().isBlank()
            ? deviceDigest(request) : body.deviceDigest();
        return ok(service.completeMfaLogin(new IamApplicationService.MfaLoginCommand(
            body.challengeNo(), body.sessionId(), deviceDigest)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code refresh}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code RefreshRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<IamApplicationService.LoginResult>}
     */
    @PostMapping("/api/iam/v1/auth/refresh")
    public ApiResponse<IamApplicationService.LoginResult> refresh(@Valid @RequestBody RefreshRequest body, HttpServletRequest request) {
        return ok(service.refresh(body.refreshToken()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code logout}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code RefreshRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/api/iam/v1/auth/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest body, HttpServletRequest request) {
        service.logout(body.refreshToken());
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code me}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param token 业务处理参数或成员，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<IamApplicationService.UserView>}
     */
    @GetMapping("/api/iam/v1/me")
    public ApiResponse<IamApplicationService.UserView> me(@RequestHeader("Authorization") String token, HttpServletRequest request) {
        return ok(service.me(token.replace("Bearer ", "")), request);
    }

    /**
     * 执行命令 {@code createUser}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code UserRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<IamApplicationService.UserView>}
     */
    @PostMapping("/api/iam/v1/users")
    public ApiResponse<IamApplicationService.UserView> createUser(@Valid @RequestBody UserRequest body, HttpServletRequest request) {
        return ok(service.createUser(body.username(), body.password()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code users}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<IamMapper.UserRow>>}
     */
    @GetMapping("/api/iam/v1/users")
    public ApiResponse<List<IamMapper.UserRow>> users(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.users(limit), request);
    }

    /**
     * 执行命令 {@code createRole}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code RoleRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<IamApplicationService.RoleView>}
     */
    @PostMapping("/api/iam/v1/roles")
    public ApiResponse<IamApplicationService.RoleView> createRole(@Valid @RequestBody RoleRequest body, HttpServletRequest request) {
        return ok(service.createRole(body.code(), body.name()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code roles}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<IamMapper.RoleRow>>}
     */
    @GetMapping("/api/iam/v1/roles")
    public ApiResponse<List<IamMapper.RoleRow>> roles(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.roles(limit), request);
    }

    /** 查询脱敏会话治理列表。 */
    @GetMapping("/api/iam/v1/sessions")
    public ApiResponse<List<com.chaobo.scm.iam.infrastructure.persistence.IamSessionMapper.SessionGovernanceRow>> sessions(
            @RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.sessions(limit), request);
    }

    @GetMapping("/api/iam/v1/role-grants")
    public ApiResponse<List<IamMapper.RoleGrantRow>> roleGrants(
            @RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.roleGrants(limit), request);
    }

    @GetMapping("/api/iam/v1/user-roles")
    public ApiResponse<List<IamMapper.UserRoleRow>> userRoles(
            @RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.userRoles(limit), request);
    }

    /**
     * 执行命令 {@code bindUserRole}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code BindUserRoleRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/api/iam/v1/roles/bind-user")
    public ApiResponse<Void> bindUserRole(@Valid @RequestBody BindUserRoleRequest body, HttpServletRequest request) {
        service.bindUserRole(body.userId(), body.roleId());
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code grantRolePermission}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code GrantPermissionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/api/iam/v1/roles/grant-permission")
    public ApiResponse<Void> grantRolePermission(@Valid @RequestBody GrantPermissionRequest body, HttpServletRequest request) {
        service.grantRolePermission(body.roleId(), body.permissionCode());
        return ok(null, request);
    }

    /**
     * 执行命令 {@code createPermission}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code PermissionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/api/iam/v1/permissions")
    public ApiResponse<Void> createPermission(@Valid @RequestBody PermissionRequest body, HttpServletRequest request) {
        service.createPermission(body.appCode(), body.code(), body.name());
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code permissions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<IamMapper.PermissionRow>>}
     */
    @GetMapping("/api/iam/v1/permissions")
    public ApiResponse<List<IamMapper.PermissionRow>> permissions(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.permissions(limit), request);
    }

    /**
     * 执行命令 {@code createDataScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code DataScopeRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/api/iam/v1/data-scopes")
    public ApiResponse<Void> createDataScope(@Valid @RequestBody DataScopeRequest body, HttpServletRequest request) {
        service.createDataScope(body.roleId(), body.type(), body.value());
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code dataScopes}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param roleId 业务或技术标识，类型为 {@code long}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<IamMapper.DataScopeRow>>}
     */
    @GetMapping("/openapi/iam/v1/data-scope")
    public ApiResponse<List<IamMapper.DataScopeRow>> dataScopes(@RequestParam long roleId, HttpServletRequest request) {
        return ok(service.dataScopes(roleId), request);
    }

    /**
     * 执行命令 {@code createApproval}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code ApprovalRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<IamApplicationService.ApprovalView>}
     */
    @PostMapping("/api/iam/v1/approval-instances")
    public ApiResponse<IamApplicationService.ApprovalView> createApproval(@Valid @RequestBody ApprovalRequest body, HttpServletRequest request) {
        return ok(service.createApproval(body.businessType(), body.businessNo()), request);
    }

    /**
     * 执行命令 {@code completeApproval}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param approvalNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code CompleteApprovalRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/api/iam/v1/approval-instances/{approvalNo}/complete")
    public ApiResponse<Void> completeApproval(@PathVariable String approvalNo, @Valid @RequestBody CompleteApprovalRequest body, HttpServletRequest request) {
        service.completeApproval(approvalNo, body.approved(), body.version());
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code approvals}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<IamMapper.ApprovalRow>>}
     */
    @GetMapping("/api/iam/v1/approval-instances")
    public ApiResponse<List<IamMapper.ApprovalRow>> approvals(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.approvals(limit), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code operationLogs}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<IamMapper.OperationLogRow>>}
     */
    @GetMapping("/api/iam/v1/operation-logs")
    public ApiResponse<List<IamMapper.OperationLogRow>> operationLogs(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.operationLogs(limit), request);
    }

    /**
     * 执行命令 {@code createSecurityPolicy}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code SecurityPolicyRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/api/iam/v1/security-policies")
    public ApiResponse<Void> createSecurityPolicy(@Valid @RequestBody SecurityPolicyRequest body, HttpServletRequest request) {
        service.createSecurityPolicy(body.code(), body.value());
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code securityPolicies}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<IamMapper.SecurityPolicyRow>>}
     */
    @GetMapping("/api/iam/v1/security-policies")
    public ApiResponse<List<IamMapper.SecurityPolicyRow>> securityPolicies(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.securityPolicies(limit), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param data 业务处理参数或成员，类型为 {@code T}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private static String deviceDigest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String remote = request.getRemoteAddr();
        return "WEB-" + Integer.toHexString(((userAgent == null ? "" : userAgent)
            + "|" + (remote == null ? "" : remote)).hashCode());
    }

    /**
     * LoginRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record LoginRequest(@NotBlank String username, @NotBlank String password,
                               @Size(max = 64) String appCode,
                               @Size(max = 128) String deviceDigest) {

        public LoginRequest(String username, String password) {
            this(username, password, null, null);
        }
    }

    /** MFA 登录完成请求。 */
    public record MfaLoginRequest(@NotBlank String challengeNo, @Positive long sessionId,
                                  @Size(max = 128) String deviceDigest) {
    }

    /**
     * RefreshRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    /**
     * UserRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record UserRequest(@NotBlank String username, @NotBlank String password) {
    }

    /**
     * RoleRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RoleRequest(@NotBlank String code, @NotBlank String name) {
    }

    /**
     * BindUserRoleRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record BindUserRoleRequest(@Positive long userId, @Positive long roleId) {
    }

    /**
     * GrantPermissionRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record GrantPermissionRequest(@Positive long roleId, @NotBlank String permissionCode) {
    }

    /**
     * PermissionRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PermissionRequest(@NotBlank String appCode, @NotBlank String code, @NotBlank String name) {
    }

    /**
     * DataScopeRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record DataScopeRequest(@Positive long roleId, @NotBlank String type, @NotBlank String value) {
    }

    /**
     * ApprovalRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ApprovalRequest(@NotBlank String businessType, @NotBlank String businessNo) {
    }

    /**
     * CompleteApprovalRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CompleteApprovalRequest(boolean approved, @PositiveOrZero int version) {
    }

    /**
     * SecurityPolicyRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record SecurityPolicyRequest(@NotBlank String code, @NotBlank String value) {
    }
}
