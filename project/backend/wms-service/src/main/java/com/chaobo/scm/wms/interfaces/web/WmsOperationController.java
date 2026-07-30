package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.operation.WmsOperationApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/**
 * WmsOperationController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/wms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:operation:write')")
public class WmsOperationController {

    /**
     * service（类型：{@code WmsOperationApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final WmsOperationApplicationService service;

    /**
     * 创建 WmsOperationController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code WmsOperationApplicationService}
     */
    public WmsOperationController(WmsOperationApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code createHandover}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreateHandover}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WmsOperationApplicationService.StatusResult>}
     */
    @PostMapping("/handovers")
    public ApiResponse<WmsOperationApplicationService.StatusResult> createHandover(@Valid @RequestBody CreateHandover body, HttpServletRequest request) {
        return ok(service.createHandover(body.handoverNo(), body.outboundId()), request);
    }

    /**
     * 执行命令 {@code confirmHandover}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Confirm}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WmsOperationApplicationService.StatusResult>}
     */
    @PostMapping("/handovers/confirm")
    public ApiResponse<WmsOperationApplicationService.StatusResult> confirmHandover(@Valid @RequestBody Confirm body, HttpServletRequest request) {
        return ok(service.confirmHandover(body.no(), body.version()), request);
    }

    /**
     * 执行命令 {@code createStocktake}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreateStocktake}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WmsOperationApplicationService.StatusResult>}
     */
    @PostMapping("/stocktakes")
    public ApiResponse<WmsOperationApplicationService.StatusResult> createStocktake(@Valid @RequestBody CreateStocktake body, HttpServletRequest request) {
        return ok(service.createStocktake(body.stocktakeNo(), body.warehouseId(), body.sku(), body.differenceQty()), request);
    }

    /**
     * 执行命令 {@code confirmStocktake}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Confirm}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WmsOperationApplicationService.StatusResult>}
     */
    @PostMapping("/stocktakes/confirm-difference")
    public ApiResponse<WmsOperationApplicationService.StatusResult> confirmStocktake(@Valid @RequestBody Confirm body, HttpServletRequest request) {
        return ok(service.confirmStocktake(body.no(), body.version()), request);
    }

    /**
     * 执行命令 {@code createException}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreateException}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WmsOperationApplicationService.StatusResult>}
     */
    @PostMapping("/warehouse-exceptions")
    public ApiResponse<WmsOperationApplicationService.StatusResult> createException(@Valid @RequestBody CreateException body, HttpServletRequest request) {
        return ok(service.createException(body.exceptionNo(), body.reason()), request);
    }

    /**
     * 执行命令 {@code closeException}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Confirm}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WmsOperationApplicationService.StatusResult>}
     */
    @PostMapping("/warehouse-exceptions/close")
    public ApiResponse<WmsOperationApplicationService.StatusResult> closeException(@Valid @RequestBody Confirm body, HttpServletRequest request) {
        return ok(service.closeException(body.no(), body.version()), request);
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
     * CreateHandover。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateHandover(@NotBlank String handoverNo, @Positive long outboundId) {
    }

    /**
     * CreateStocktake。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateStocktake(@NotBlank String stocktakeNo, @Positive long warehouseId, @NotBlank String sku, @NotNull @DecimalMin("0") BigDecimal differenceQty) {
    }

    /**
     * CreateException。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateException(@NotBlank String exceptionNo, @NotBlank String reason) {
    }

    /**
     * Confirm。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Confirm(@NotBlank String no, @PositiveOrZero int version) {
    }
}
