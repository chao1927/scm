package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.receiving.ReceivingApplicationService;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;

/**
 * ReceivingController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/wms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:receiving:write')")
public class ReceivingController {

    /**
     * service（类型：{@code ReceivingApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final ReceivingApplicationService service;

    /**
     * 创建 ReceivingController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code ReceivingApplicationService}
     */
    public ReceivingController(ReceivingApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code open}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code OpenRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<ReceivingApplicationService.Result>}
     */
    @PostMapping("/receipts")
    public ApiResponse<ReceivingApplicationService.Result> open(@Valid @RequestBody OpenRequest body, HttpServletRequest request, Authentication authentication) {
        var command = new ReceivingApplicationService.Open(body.receiptNo(), body.inboundId(), body.skuCode(), body.expectedQty());
        return ok(service.open(command, WmsAccessControl.operatorId(authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code scan}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code ScanRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<ReceivingApplicationService.Result>}
     */
    @PostMapping("/pda/receipts/scan")
    public ApiResponse<ReceivingApplicationService.Result> scan(@Valid @RequestBody ScanRequest body, HttpServletRequest request, Authentication authentication) {
        var command = new ReceivingApplicationService.Scan(body.receiptNo(), body.version(), body.receivedQty(), body.rejectedQty(), body.rejectReason(), request.getHeader("X-Idempotency-Key"));
        return ok(service.scan(command, WmsAccessControl.operatorId(authentication)), request);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param receiptNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<ReceivingApplicationService.Result>}
     */
    @PostMapping("/receipts/{receiptNo}/submit")
    public ApiResponse<ReceivingApplicationService.Result> submit(@PathVariable String receiptNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(service.submit(receiptNo, body.version(), WmsAccessControl.operatorId(authentication)), request);
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
     * OpenRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record OpenRequest(@NotBlank String receiptNo, @Positive long inboundId, @NotBlank String skuCode, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal expectedQty) {
    }

    /**
     * ScanRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ScanRequest(@NotBlank String receiptNo, @PositiveOrZero int version, @NotNull @DecimalMin("0") BigDecimal receivedQty, @NotNull @DecimalMin("0") BigDecimal rejectedQty, String rejectReason) {
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
}
