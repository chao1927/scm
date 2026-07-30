package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.purchase.application.order.*;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * PurchaseOrderController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/purchase/v1/purchase-orders")
public class PurchaseOrderController {

    /**
     * applicationService（类型：{@code PurchaseOrderApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderApplicationService applicationService;

    /**
     * queryService（类型：{@code PurchaseOrderQueryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderQueryApplicationService queryService;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 PurchaseOrderController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param applicationService 应用或外部协作依赖，类型为 {@code PurchaseOrderApplicationService}
     * @param queryService 应用或外部协作依赖，类型为 {@code PurchaseOrderQueryApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public PurchaseOrderController(PurchaseOrderApplicationService applicationService, PurchaseOrderQueryApplicationService queryService, CommandContextFactory contexts) {
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
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<PurchaseOrderView>>}
     */
    @GetMapping
    public ApiResponse<PageResult<PurchaseOrderView>> page(@RequestParam(required = false) Long purchaseOrgId, @RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, HttpServletRequest request) {
        return ok(queryService.page(purchaseOrgId, scope(request), supplierId, status, pageNo, pageSize), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PurchaseOrderView>}
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<PurchaseOrderView> detail(@PathVariable String orderNo, HttpServletRequest request) {
        return ok(queryService.detail(orderNo, scope(request)), request);
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
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{orderNo}/submit")
    public ApiResponse<CommandResult> submit(@PathVariable String orderNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.submit(orderNo, new PurchaseOrderCommands.Version(body.version()), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code ApproveRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{orderNo}/approve")
    public ApiResponse<CommandResult> approve(@PathVariable String orderNo, @Valid @RequestBody ApproveRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.approve(orderNo, new PurchaseOrderCommands.Approve(body.version(), body.approved(), body.reason()), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code PublishRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{orderNo}/publish")
    public ApiResponse<CommandResult> publish(@PathVariable String orderNo, @Valid @RequestBody PublishRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.publish(orderNo, new PurchaseOrderCommands.Publish(body.version(), body.publishMode()), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code CancelRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<CommandResult> cancel(@PathVariable String orderNo, @Valid @RequestBody CancelRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.cancel(orderNo, new PurchaseOrderCommands.Cancel(body.version(), body.reason()), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code CloseRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{orderNo}/close")
    public ApiResponse<CommandResult> close(@PathVariable String orderNo, @Valid @RequestBody CloseRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.close(orderNo, new PurchaseOrderCommands.Close(body.version(), body.reason()), contexts.create(request, authentication)), request);
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
    public record CreateRequest(@Min(1) @Max(4) int purchaseType, @Positive long supplierId, @NotBlank String supplierCode, @NotBlank String supplierName, @Positive long purchaseOrgId, String warehouseCode, @NotBlank String currency, @NotEmpty List<@Valid LineRequest> lines) {

        /**
         * 转换数据模型 {@code toCommand}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code PurchaseOrderCommands.Create}
         */
        PurchaseOrderCommands.Create toCommand() {
            return new PurchaseOrderCommands.Create(purchaseType, supplierId, supplierCode, supplierName, purchaseOrgId, warehouseCode, currency, lines.stream().map(LineRequest::toCommand).toList());
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
    public record LineRequest(Long lineId, @NotBlank String skuCode, String skuName, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal orderQty, @NotNull @DecimalMin("0") BigDecimal unitPrice, @NotNull @DecimalMin("0") BigDecimal taxRate, LocalDate requiredDeliveryDate) {

        /**
         * 转换数据模型 {@code toCommand}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code PurchaseOrderCommands.Line}
         */
        PurchaseOrderCommands.Line toCommand() {
            return new PurchaseOrderCommands.Line(lineId, skuCode, skuName, orderQty, unitPrice, taxRate, requiredDeliveryDate);
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
     * PublishRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PublishRequest(@PositiveOrZero int version, String publishMode) {
    }

    /**
     * CancelRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CancelRequest(@PositiveOrZero int version, @NotBlank String reason) {
    }

    /**
     * CloseRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CloseRequest(@PositiveOrZero int version, @NotBlank String reason) {
    }
}
