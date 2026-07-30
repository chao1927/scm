package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.transfer.TransferOperationApplicationService;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

/**
 * TransferOperationController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/wms/v1/transfer-operations")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:transfer:write')")
public class TransferOperationController {

    /**
     * service（类型：{@code TransferOperationApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final TransferOperationApplicationService service;

    /**
     * 创建 TransferOperationController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code TransferOperationApplicationService}
     */
    public TransferOperationController(TransferOperationApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code outbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code Quantity}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<TransferOperationApplicationService.Result>}
     */
    @PostMapping("/{transferNo}/outbound")
    public ApiResponse<TransferOperationApplicationService.Result> outbound(@PathVariable String transferNo, @Valid @RequestBody Quantity body, HttpServletRequest request, Authentication authentication) {
        var transfer = service.detail(transferNo);
        WmsAccessControl.requireWarehouse(authentication, transfer.sourceWarehouseId());
        return ok(service.completeOutbound(transferNo, body.qty(), body.version()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code Receive}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<TransferOperationApplicationService.Result>}
     */
    @PostMapping("/{transferNo}/receive")
    public ApiResponse<TransferOperationApplicationService.Result> receive(@PathVariable String transferNo, @Valid @RequestBody Receive body, HttpServletRequest request, Authentication authentication) {
        var transfer = service.detail(transferNo);
        WmsAccessControl.requireWarehouse(authentication, transfer.targetWarehouseId());
        return ok(service.receive(transferNo, body.qty(), body.finalReceipt(), body.version()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<TransferOperationApplicationService.Result>}
     */
    @GetMapping("/{transferNo}")
    public ApiResponse<TransferOperationApplicationService.Result> detail(@PathVariable String transferNo, HttpServletRequest request, Authentication authentication) {
        var result = service.detail(transferNo);
        WmsAccessControl.requireWarehouse(authentication, result.sourceWarehouseId());
        return ok(result, request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code T}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    private static <T> ApiResponse<T> ok(T value, HttpServletRequest request) {
        return ApiResponse.success(value, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * Quantity。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Quantity(@NotNull @Positive BigDecimal qty, @PositiveOrZero int version) {
    }

    /**
     * Receive。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Receive(@NotNull @Positive BigDecimal qty, boolean finalReceipt, @PositiveOrZero int version) {
    }
}
