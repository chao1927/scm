package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.inbound.InboundOrderApplicationService;
import com.chaobo.scm.wms.application.inbound.WmsCommandResult;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

/**
 * InboundOrderController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:inbound:write')")
public class InboundOrderController {

    /**
     * service（类型：{@code InboundOrderApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundOrderApplicationService service;

    /**
     * 创建 InboundOrderController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code InboundOrderApplicationService}
     */
    public InboundOrderController(InboundOrderApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreateRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WmsCommandResult>}
     */
    @PostMapping("/openapi/wms/v1/inbound-orders")
    public ApiResponse<WmsCommandResult> create(@Valid @RequestBody CreateRequest body, HttpServletRequest request, Authentication authentication) {
        var source = request.getHeader("X-Source-System");
        var idempotencyKey = request.getHeader("X-Idempotency-Key");
        if (source == null || source.isBlank()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少入库事实来源系统");
        }
        WmsAccessControl.requireWarehouse(authentication, body.warehouseId());
        WmsAccessControl.requireOwner(authentication, body.ownerId());
        return ok(service.create(new InboundOrderApplicationService.Create(source, body.inboundType(),
            body.sourceNo(), body.sourceLineNo(), body.warehouseId(), body.ownerId(), body.allowedQty(),
            body.expectedArrivalAt(), idempotencyKey), WmsAccessControl.operatorId(authentication)), request);
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code CancelRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WmsCommandResult>}
     */
    @PostMapping("/api/wms/v1/inbound-orders/{id}/cancel")
    public ApiResponse<WmsCommandResult> cancel(@PathVariable long id, @Valid @RequestBody CancelRequest body, HttpServletRequest request, Authentication authentication) {
        long warehouseId = warehouseScope(request);
        WmsAccessControl.requireWarehouse(authentication, warehouseId);
        return ok(service.cancel(id, new InboundOrderApplicationService.Cancel(body.version(), body.reason()), warehouseId, WmsAccessControl.operatorId(authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code warehouseScope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    private static long warehouseScope(HttpServletRequest request) {
        var value = request.getHeader("X-Warehouse-Id");
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少仓库范围");
        }
        return Long.parseLong(value);
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
     * CreateRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateRequest(@NotBlank String inboundType, @NotBlank String sourceNo,
                                @NotBlank String sourceLineNo, @Positive long warehouseId,
                                @Positive long ownerId, @jakarta.validation.constraints.NotNull
                                @jakarta.validation.constraints.DecimalMin("0.000001") BigDecimal allowedQty,
                                OffsetDateTime expectedArrivalAt) {
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
