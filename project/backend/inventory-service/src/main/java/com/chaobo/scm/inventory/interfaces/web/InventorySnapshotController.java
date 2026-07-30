package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.inventory.application.InventorySnapshotApplicationService;
import com.chaobo.scm.inventory.infrastructure.persistence.InventorySnapshotMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * InventorySnapshotController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/inventory/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'inventory:*', 'inventory:reconciliation:manage')")
public class InventorySnapshotController {

    /**
     * service（类型：{@code InventorySnapshotApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InventorySnapshotApplicationService service;

    /**
     * 创建 InventorySnapshotController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code InventorySnapshotApplicationService}
     */
    public InventorySnapshotController(InventorySnapshotApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code generate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code AccountRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<InventorySnapshotApplicationService.SnapshotResult>}
     */
    @PostMapping("/snapshots/generate")
    public ApiResponse<InventorySnapshotApplicationService.SnapshotResult> generate(@Valid @RequestBody AccountRequest body, HttpServletRequest request) {
        return ok(service.generate(body.accountId()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshots}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<InventorySnapshotMapper.SnapshotRow>>}
     */
    @GetMapping("/snapshots")
    public ApiResponse<List<InventorySnapshotMapper.SnapshotRow>> snapshots(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.snapshots(limit), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code reconcile}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code ReconcileRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<InventorySnapshotApplicationService.ReconcileResult>}
     */
    @PostMapping("/inventory-reconciliations")
    public ApiResponse<InventorySnapshotApplicationService.ReconcileResult> reconcile(@Valid @RequestBody ReconcileRequest body, HttpServletRequest request) {
        return ok(service.createReconcile(body.accountId(), body.wmsQty()), request);
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reconcileNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<InventorySnapshotApplicationService.ReconcileResult>}
     */
    @PostMapping("/inventory-reconciliations/{reconcileNo}/confirm")
    public ApiResponse<InventorySnapshotApplicationService.ReconcileResult> confirm(@PathVariable String reconcileNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request) {
        return ok(service.confirm(reconcileNo, body.version()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code reconciles}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<InventorySnapshotMapper.ReconcileRow>>}
     */
    @GetMapping("/inventory-reconciliations")
    public ApiResponse<List<InventorySnapshotMapper.ReconcileRow>> reconciles(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.reconciles(limit), request);
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
     * AccountRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record AccountRequest(@Positive long accountId) {
    }

    /**
     * ReconcileRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReconcileRequest(@Positive long accountId, @NotNull BigDecimal wmsQty) {
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
