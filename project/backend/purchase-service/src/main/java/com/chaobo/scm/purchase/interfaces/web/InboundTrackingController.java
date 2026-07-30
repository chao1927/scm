package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.inbound.InboundCommands;
import com.chaobo.scm.purchase.application.inbound.InboundTrackingApplicationService;
import com.chaobo.scm.purchase.application.inbound.InboundTrackingQueryApplicationService;
import com.chaobo.scm.purchase.application.inbound.InboundTrackingView;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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

/**
 * InboundTrackingController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/purchase/v1/inbounds")
public class InboundTrackingController {

    /**
     * applicationService（类型：{@code InboundTrackingApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundTrackingApplicationService applicationService;

    /**
     * queryService（类型：{@code InboundTrackingQueryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundTrackingQueryApplicationService queryService;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 InboundTrackingController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param applicationService 应用或外部协作依赖，类型为 {@code InboundTrackingApplicationService}
     * @param queryService 应用或外部协作依赖，类型为 {@code InboundTrackingQueryApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public InboundTrackingController(InboundTrackingApplicationService applicationService, InboundTrackingQueryApplicationService queryService, CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<InboundTrackingView>>}
     */
    @GetMapping
    public ApiResponse<PageResult<InboundTrackingView>> page(@RequestParam(required = false) Long purchaseOrgId, @RequestParam(required = false) String orderNo, @RequestParam(required = false) String asnNo, @RequestParam(required = false) String warehouseCode, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, HttpServletRequest request) {
        return ok(queryService.page(purchaseOrgId, scope(request), orderNo, asnNo, warehouseCode, status, pageNo, pageSize), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<InboundTrackingView>}
     */
    @GetMapping("/{inboundNo}")
    public ApiResponse<InboundTrackingView> detail(@PathVariable String inboundNo, HttpServletRequest request) {
        return ok(queryService.detail(inboundNo, scope(request)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code recordAsn}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code RecordAsnRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/asns")
    public ApiResponse<CommandResult> recordAsn(@Valid @RequestBody RecordAsnRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.recordAsn(body.toCommand(), contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code syncWms}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code SyncWmsRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{inboundNo}/sync-wms")
    public ApiResponse<CommandResult> syncWms(@PathVariable String inboundNo, @Valid @RequestBody SyncWmsRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.syncWms(inboundNo, body.toCommand(), contexts.create(request, authentication)), request);
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
     * RecordAsnRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RecordAsnRequest(@NotBlank String orderNo, @NotBlank String asnNo, @Positive long supplierId, @Positive long purchaseOrgId, String warehouseCode, @NotBlank String skuCode, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal notifiedQty) {

        /**
         * 转换数据模型 {@code toCommand}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code InboundCommands.RecordAsn}
         */
        InboundCommands.RecordAsn toCommand() {
            return new InboundCommands.RecordAsn(orderNo, asnNo, supplierId, purchaseOrgId, warehouseCode, skuCode, notifiedQty);
        }
    }

    /**
     * SyncWmsRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record SyncWmsRequest(@PositiveOrZero int version, @NotNull @DecimalMin("0") BigDecimal receivedQty, @NotNull @DecimalMin("0") BigDecimal qualifiedQty, @NotNull @DecimalMin("0") BigDecimal unqualifiedQty, @NotNull @DecimalMin("0") BigDecimal putawayQty, String reason) {

        /**
         * 转换数据模型 {@code toCommand}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code InboundCommands.SyncWms}
         */
        InboundCommands.SyncWms toCommand() {
            return new InboundCommands.SyncWms(version, receivedQty, qualifiedQty, unqualifiedQty, putawayQty, reason);
        }
    }
}
