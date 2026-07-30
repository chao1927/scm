package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.application.supplierreturn.SupplierReturnApplicationService;
import com.chaobo.scm.purchase.application.supplierreturn.SupplierReturnCommands;
import com.chaobo.scm.purchase.application.supplierreturn.SupplierReturnQueryApplicationService;
import com.chaobo.scm.purchase.application.supplierreturn.SupplierReturnView;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.List;

/**
 * SupplierReturnController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/purchase/v1/supplier-returns")
public class SupplierReturnController {

    /**
     * applicationService（类型：{@code SupplierReturnApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReturnApplicationService applicationService;

    /**
     * queryService（类型：{@code SupplierReturnQueryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierReturnQueryApplicationService queryService;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierReturnController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param applicationService 应用或外部协作依赖，类型为 {@code SupplierReturnApplicationService}
     * @param queryService 应用或外部协作依赖，类型为 {@code SupplierReturnQueryApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierReturnController(SupplierReturnApplicationService applicationService, SupplierReturnQueryApplicationService queryService, CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<SupplierReturnView>>}
     */
    @GetMapping
    public ApiResponse<PageResult<SupplierReturnView>> page(@RequestParam(required = false) Long purchaseOrgId, @RequestParam(required = false) Long supplierId, @RequestParam(required = false) String warehouseCode, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, HttpServletRequest request) {
        return ok(queryService.page(purchaseOrgId, scope(request), supplierId, warehouseCode, status, pageNo, pageSize), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierReturnView>}
     */
    @GetMapping("/{returnNo}")
    public ApiResponse<SupplierReturnView> detail(@PathVariable String returnNo, HttpServletRequest request) {
        return ok(queryService.detail(returnNo, scope(request)), request);
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreateRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping
    public ApiResponse<CommandResult> create(@Valid @RequestBody CreateRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.create(body.toCommand(), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{returnNo}/submit")
    public ApiResponse<CommandResult> submit(@PathVariable String returnNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.submit(returnNo, new SupplierReturnCommands.Version(body.version()), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code ApproveRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{returnNo}/approve")
    public ApiResponse<CommandResult> approve(@PathVariable String returnNo, @Valid @RequestBody ApproveRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.approve(returnNo, new SupplierReturnCommands.Approve(body.version(), body.approved(), body.reason()), contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code notifyExecution}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code NotifyRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{returnNo}/notify-execution")
    public ApiResponse<CommandResult> notifyExecution(@PathVariable String returnNo, @Valid @RequestBody NotifyRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.notifyExecution(returnNo, new SupplierReturnCommands.Notify(body.version(), body.notifyMode()), contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param data 业务处理参数或成员，类型为 {@code T}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code scope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long scope(HttpServletRequest request) {
        var value = request.getHeader("X-Purchase-Org-Id");
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    /**
     * CreateRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateRequest(@NotBlank String sourceOrderNo, @Positive long supplierId, @Positive long purchaseOrgId, String warehouseCode, @NotEmpty List<@Valid LineRequest> lines) {

        /**
         * 转换数据模型 {@code toCommand}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code SupplierReturnCommands.Create}
         */
        SupplierReturnCommands.Create toCommand() {
            return new SupplierReturnCommands.Create(sourceOrderNo, supplierId, purchaseOrgId, warehouseCode, lines.stream().map(LineRequest::toCommand).toList());
        }
    }

    /**
     * LineRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record LineRequest(Long lineId, @NotBlank String skuCode, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal returnQty, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal returnableQty, String reason) {

        /**
         * 转换数据模型 {@code toCommand}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code SupplierReturnCommands.Line}
         */
        SupplierReturnCommands.Line toCommand() {
            return new SupplierReturnCommands.Line(lineId, skuCode, returnQty, returnableQty, reason);
        }
    }

    /**
     * VersionRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record VersionRequest(@PositiveOrZero int version) {
    }

    /**
     * ApproveRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ApproveRequest(@PositiveOrZero int version, boolean approved, String reason) {
    }

    /**
     * NotifyRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record NotifyRequest(@PositiveOrZero int version, String notifyMode) {
    }
}
