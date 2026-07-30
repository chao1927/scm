package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamPermissionOpenApiApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IamPermissionOpenApiController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/openapi/iam/v1")
public class IamPermissionOpenApiController {

    /**
     * service（类型：{@code IamPermissionOpenApiApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamPermissionOpenApiApplicationService service;

    /**
     * 创建 IamPermissionOpenApiController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code IamPermissionOpenApiApplicationService}
     */
    public IamPermissionOpenApiController(IamPermissionOpenApiApplicationService service) {
        this.service = service;
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code IamPermissionOpenApiApplicationService.TokenValidationCommand}
     * @return 校验业务约束的结果，类型为 {@code IamPermissionOpenApiApplicationService.TokenValidationResult}
     */
    @PostMapping("/tokens/validate")
    public IamPermissionOpenApiApplicationService.TokenValidationResult validate(@RequestBody IamPermissionOpenApiApplicationService.TokenValidationCommand command) {
        return service.validateToken(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code permissions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param authorization 业务处理参数或成员，类型为 {@code String}
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamPermissionOpenApiApplicationService.PermissionSnapshot}
     */
    @GetMapping("/users/me/permissions")
    public IamPermissionOpenApiApplicationService.PermissionSnapshot permissions(@RequestHeader("Authorization") String authorization, @RequestHeader(value = "X-App-Code", required = false) String appCode) {
        return service.snapshot(authorization.replace("Bearer ", ""), appCode);
    }

    /**
     * 校验业务约束 {@code check}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code IamPermissionOpenApiApplicationService.PermissionCheckCommand}
     * @return 校验业务约束的结果，类型为 {@code IamPermissionOpenApiApplicationService.PermissionCheckResult}
     */
    @PostMapping("/permissions/check")
    public IamPermissionOpenApiApplicationService.PermissionCheckResult check(@RequestBody IamPermissionOpenApiApplicationService.PermissionCheckCommand command) {
        return service.checkPermission(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code resolve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code IamPermissionOpenApiApplicationService.DataScopeResolveCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamPermissionOpenApiApplicationService.DataScopeResolveResult}
     */
    @PostMapping("/data-scopes/resolve")
    public IamPermissionOpenApiApplicationService.DataScopeResolveResult resolve(@RequestBody IamPermissionOpenApiApplicationService.DataScopeResolveCommand command) {
        return service.resolveDataScope(command);
    }
}
