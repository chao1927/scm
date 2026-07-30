package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.profile.SupplierAdmissionApplicationService;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.*;
import org.springframework.web.bind.annotation.*;

/**
 * SupplierAdmissionController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/admissions")
public class SupplierAdmissionController {

    /**
     * service（类型：{@code SupplierAdmissionApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierAdmissionApplicationService service;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierAdmissionController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierAdmissionApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierAdmissionController(SupplierAdmissionApplicationService service, CommandContextFactory contexts) {
        this.service = service;
        this.contexts = contexts;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Create}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param auth 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping
    public ApiResponse<CommandResult> create(@Valid @RequestBody Create body, HttpServletRequest request, Authentication auth) {
        return ok(service.create(body.supplierCode(), body.supplierName(), body.taxNo(), body.supplierType(), body.contactName(), body.contactMobile(), body.settlementJson(), contexts.create(request, auth)), request);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Version}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param auth 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/submit")
    public ApiResponse<CommandResult> submit(@PathVariable long id, @Valid @RequestBody Version body, HttpServletRequest request, Authentication auth) {
        return ok(service.submit(id, body.version(), contexts.create(request, auth)), request);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Version}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param auth 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<CommandResult> approve(@PathVariable long id, @Valid @RequestBody Version body, HttpServletRequest request, Authentication auth) {
        return ok(service.approve(id, body.version(), contexts.create(request, auth)), request);
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Reject}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param auth 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/reject")
    public ApiResponse<CommandResult> reject(@PathVariable long id, @Valid @RequestBody Reject body, HttpServletRequest request, Authentication auth) {
        return ok(service.reject(id, body.version(), body.reason(), contexts.create(request, auth)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code CommandResult}
     * @param q 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    private ApiResponse<CommandResult> ok(CommandResult r, HttpServletRequest q) {
        return ApiResponse.success(r, q.getHeader("X-Request-Id"), q.getHeader("X-Trace-Id"));
    }

    /**
     * Create。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Create(@NotBlank String supplierCode, @NotBlank String supplierName, @NotBlank String taxNo, @NotBlank String supplierType, @NotBlank String contactName, @NotBlank String contactMobile, @NotBlank String settlementJson) {
    }

    /**
     * Version。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Version(@PositiveOrZero int version) {
    }

    /**
     * Reject。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Reject(@PositiveOrZero int version, @NotBlank String reason) {
    }
}
