package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.outbound.OutboundApplicationService;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

/**
 * OutboundOrderController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:outbound:write')")
public class OutboundOrderController {

    /**
     * service（类型：{@code OutboundApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final OutboundApplicationService service;

    /**
     * 创建 OutboundOrderController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code OutboundApplicationService}
     */
    public OutboundOrderController(OutboundApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Create}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<OutboundApplicationService.Result>}
     */
    @PostMapping("/openapi/wms/v1/outbound-orders")
    public ApiResponse<OutboundApplicationService.Result> create(@Valid @RequestBody Create body, HttpServletRequest request, Authentication authentication) {
        var source = request.getHeader("X-Source-System");
        if (source == null || !source.equals(body.sourceType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "来源系统与出库来源不一致");
        }
        if (request.getHeader(HEADER_X_IDEMPOTENCY_KEY) == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少幂等键");
        }
        WmsAccessControl.requireWarehouse(authentication, body.warehouseId());
        WmsAccessControl.requireOwner(authentication, body.ownerId());
        return ok(service.create(body.sourceType(), body.sourceNo(), body.warehouseId(), body.ownerId(),
            WmsAccessControl.operatorId(authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code allocate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Change}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<OutboundApplicationService.Result>}
     */
    @PostMapping("/api/wms/v1/outbound-orders/allocate")
    public ApiResponse<OutboundApplicationService.Result> allocate(@Valid @RequestBody Change body, HttpServletRequest request, Authentication authentication) {
        WmsAccessControl.requireWarehouse(authentication, body.warehouseId());
        WmsAccessControl.requireOwner(authentication, body.ownerId());
        return ok(service.allocate(body.sourceType(), body.sourceNo(), body.warehouseId(), body.ownerId(),
            body.version(), WmsAccessControl.operatorId(authentication)), request);
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Cancel}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<OutboundApplicationService.Result>}
     */
    @PostMapping("/openapi/wms/v1/outbound-orders/cancel")
    public ApiResponse<OutboundApplicationService.Result> cancel(@Valid @RequestBody Cancel body, HttpServletRequest request, Authentication authentication) {
        WmsAccessControl.requireWarehouse(authentication, body.warehouseId());
        WmsAccessControl.requireOwner(authentication, body.ownerId());
        return ok(service.cancel(body.sourceType(), body.sourceNo(), body.warehouseId(), body.ownerId(),
            body.version(), body.reason(), WmsAccessControl.operatorId(authentication)), request);
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

    /**
     * Create。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Create(@NotBlank String sourceType, @NotBlank String sourceNo,
                         @Positive long warehouseId, @Positive long ownerId) {
    }

    /**
     * Change。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Change(@NotBlank String sourceType, @NotBlank String sourceNo,
                         @Positive long warehouseId, @Positive long ownerId,
                         @PositiveOrZero int version) {
    }

    /**
     * Cancel。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Cancel(@NotBlank String sourceType, @NotBlank String sourceNo,
                         @Positive long warehouseId, @Positive long ownerId,
                         @PositiveOrZero int version, @NotBlank String reason) {
    }

    /**
     * 业务常量 {@code HEADER_X_IDEMPOTENCY_KEY}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String HEADER_X_IDEMPOTENCY_KEY = "X-Idempotency-Key";
}
