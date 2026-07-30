package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.supplierconfirm.SupplierConfirmApplicationService;
import com.chaobo.scm.purchase.application.supplierconfirm.SupplierConfirmCommands;
import com.chaobo.scm.purchase.application.supplierconfirm.SupplierConfirmView;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SupplierConfirmController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/purchase/v1/supplier-confirms")
public class SupplierConfirmController {

    /**
     * service（类型：{@code SupplierConfirmApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierConfirmApplicationService service;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierConfirmController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierConfirmApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierConfirmController(SupplierConfirmApplicationService service, CommandContextFactory contexts) {
        this.service = service;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param processedStatus 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<SupplierConfirmView>>}
     */
    @GetMapping
    public ApiResponse<PageResult<SupplierConfirmView>> page(@RequestParam(required = false) Long purchaseOrgId, @RequestParam(required = false) String orderNo, @RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer processedStatus, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, HttpServletRequest request, Authentication authentication) {
        var context = contexts.create(request, authentication);
        return ok(service.page(purchaseOrgId, context.purchaseOrgScope(), orderNo, supplierId, processedStatus, pageNo, pageSize, context), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierConfirmView>}
     */
    @GetMapping("/{confirmId}")
    public ApiResponse<SupplierConfirmView> detail(@PathVariable long confirmId, HttpServletRequest request, Authentication authentication) {
        var context = contexts.create(request, authentication);
        return ok(service.detail(confirmId, context.purchaseOrgScope(), context), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code acceptDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code ProcessRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/{confirmId}/accept-diff")
    public ApiResponse<Void> acceptDifference(@PathVariable long confirmId, @Valid @RequestBody ProcessRequest body, HttpServletRequest request, Authentication authentication) {
        service.acceptDifference(confirmId, new SupplierConfirmCommands.Process(body.version(), body.comment()), contexts.create(request, authentication));
        return ok(null, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code renegotiate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code RenegotiateRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/{confirmId}/renegotiate")
    public ApiResponse<Void> renegotiate(@PathVariable long confirmId, @Valid @RequestBody RenegotiateRequest body, HttpServletRequest request, Authentication authentication) {
        service.renegotiate(confirmId, new SupplierConfirmCommands.Renegotiate(body.version(), body.requirement(), body.comment()), contexts.create(request, authentication));
        return ok(null, request);
    }

    /**
     * 执行命令 {@code cancelOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code CancelRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/{confirmId}/cancel-order")
    public ApiResponse<Void> cancelOrder(@PathVariable long confirmId, @Valid @RequestBody CancelRequest body, HttpServletRequest request, Authentication authentication) {
        service.cancelOrder(confirmId, new SupplierConfirmCommands.CancelOrder(body.version(), body.reason()), contexts.create(request, authentication));
        return ok(null, request);
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
     * ProcessRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ProcessRequest(@PositiveOrZero int version, String comment) {
    }

    /**
     * RenegotiateRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RenegotiateRequest(@PositiveOrZero int version, @NotBlank String requirement, String comment) {
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
}
