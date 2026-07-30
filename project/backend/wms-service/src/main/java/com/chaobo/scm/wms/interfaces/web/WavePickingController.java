package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.wave.WavePickingApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

/**
 * WavePickingController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/wms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:picking:write')")
public class WavePickingController {

    /**
     * service（类型：{@code WavePickingApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final WavePickingApplicationService service;

    /**
     * 创建 WavePickingController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code WavePickingApplicationService}
     */
    public WavePickingController(WavePickingApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code createWave}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreateWave}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WavePickingApplicationService.WaveResult>}
     */
    @PostMapping("/waves")
    public ApiResponse<WavePickingApplicationService.WaveResult> createWave(@Valid @RequestBody CreateWave body, HttpServletRequest request) {
        return ok(service.createWave(body.waveNo(), body.warehouseId()), request);
    }

    /**
     * 执行命令 {@code releaseWave}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code ReleaseWave}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WavePickingApplicationService.WaveResult>}
     */
    @PostMapping("/waves/release")
    public ApiResponse<WavePickingApplicationService.WaveResult> releaseWave(@Valid @RequestBody ReleaseWave body, HttpServletRequest request) {
        return ok(service.releaseWave(body.waveNo(), body.version()), request);
    }

    /**
     * 执行命令 {@code createPickTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreatePickTask}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WavePickingApplicationService.PickResult>}
     */
    @PostMapping("/pick-tasks")
    public ApiResponse<WavePickingApplicationService.PickResult> createPickTask(@Valid @RequestBody CreatePickTask body, HttpServletRequest request) {
        return ok(service.createPickTask(body.taskNo(), body.waveId(), body.outboundId(), body.sku(), body.requiredQty()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code scanPick}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code ScanPick}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<WavePickingApplicationService.PickResult>}
     */
    @PostMapping("/pda/pick-tasks/scan")
    public ApiResponse<WavePickingApplicationService.PickResult> scanPick(@Valid @RequestBody ScanPick body, HttpServletRequest request) {
        return ok(service.scanPick(body.taskNo(), body.version(), body.qty()), request);
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
     * CreateWave。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateWave(@NotBlank String waveNo, @Positive long warehouseId) {
    }

    /**
     * ReleaseWave。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReleaseWave(@NotBlank String waveNo, @PositiveOrZero int version) {
    }

    /**
     * CreatePickTask。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreatePickTask(@NotBlank String taskNo, @Positive long waveId, @Positive long outboundId, @NotBlank String sku, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal requiredQty) {
    }

    /**
     * ScanPick。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ScanPick(@NotBlank String taskNo, @PositiveOrZero int version, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal qty) {
    }
}
