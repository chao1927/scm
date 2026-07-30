package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.inventory.application.InventoryApplicationService;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import com.chaobo.scm.inventory.infrastructure.security.InventoryAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.util.List;

/**
 * InventoryController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'inventory:*', 'inventory:stock:write')")
public class InventoryController {

    /**
     * service（类型：{@code InventoryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InventoryApplicationService service;

    /**
     * 创建 InventoryController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code InventoryApplicationService}
     */
    public InventoryController(InventoryApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code stocks}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<InventoryMapper.AccountRow>>}
     */
    @GetMapping("/api/inventory/v1/stocks")
    public ApiResponse<List<InventoryMapper.AccountRow>> stocks(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.stocks(limit), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ledgers}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<InventoryMapper.LedgerRow>>}
     */
    @GetMapping("/api/inventory/v1/stock-ledgers")
    public ApiResponse<List<InventoryMapper.LedgerRow>> ledgers(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.ledgers(limit), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code inbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code AccountRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<InventoryApplicationService.AccountResult>}
     */
    @PostMapping("/openapi/inventory/v1/wms/inbound")
    public ApiResponse<InventoryApplicationService.AccountResult> inbound(@Valid @RequestBody AccountRequest body, HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.inbound(command(body, "WMS")), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code outbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code AccountRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<InventoryApplicationService.AccountResult>}
     */
    @PostMapping("/openapi/inventory/v1/wms/outbound")
    public ApiResponse<InventoryApplicationService.AccountResult> outbound(@Valid @RequestBody AccountRequest body, HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.outbound(command(body, "WMS")), request);
    }

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code AccountRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<InventoryApplicationService.ReservationResult>}
     */
    @PostMapping("/openapi/inventory/v1/reservations")
    public ApiResponse<InventoryApplicationService.ReservationResult> reserve(@Valid @RequestBody AccountRequest body, HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.reserve(new InventoryApplicationService.ReservationCommand(body.ownerId(), body.warehouseId(), body.sku(), body.batchNo(), body.qty(), source(request), body.sourceNo())), request);
    }

    /**
     * 执行命令 {@code release}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reservationNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<InventoryApplicationService.ReservationResult>}
     */
    @PostMapping("/openapi/inventory/v1/reservations/{reservationNo}/release")
    public ApiResponse<InventoryApplicationService.ReservationResult> release(@PathVariable String reservationNo, HttpServletRequest request) {
        return ok(service.release(reservationNo), request);
    }

    /**
     * 执行命令 {@code freeze}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code AccountRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<InventoryApplicationService.AccountResult>}
     */
    @PostMapping("/api/inventory/v1/freezes")
    public ApiResponse<InventoryApplicationService.AccountResult> freeze(@Valid @RequestBody AccountRequest body, HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.freeze(command(body, "INVENTORY")), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code unfreeze}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code AccountRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<InventoryApplicationService.AccountResult>}
     */
    @PostMapping("/api/inventory/v1/freezes/unfreeze")
    public ApiResponse<InventoryApplicationService.AccountResult> unfreeze(@Valid @RequestBody AccountRequest body, HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.unfreeze(command(body, "INVENTORY")), request);
    }

    /**
     * 执行命令 {@code adjust}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code AccountRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<InventoryApplicationService.AccountResult>}
     */
    @PostMapping("/api/inventory/v1/adjustments")
    public ApiResponse<InventoryApplicationService.AccountResult> adjust(@Valid @RequestBody AccountRequest body, HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.adjust(command(body, "INVENTORY")), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code command}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param body 业务处理参数或成员，类型为 {@code AccountRequest}
     * @param defaultSource 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InventoryApplicationService.AccountCommand}
     */
    private static InventoryApplicationService.AccountCommand command(AccountRequest body, String defaultSource) {
        return new InventoryApplicationService.AccountCommand(body.ownerId(), body.warehouseId(), body.sku(), body.batchNo(), body.qty(), defaultSource, body.sourceNo());
    }

    /**
     * 处理当前类型职责中的操作 {@code source}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String source(HttpServletRequest request) {
        var value = request.getHeader("X-Source-System");
        return value == null || value.isBlank() ? "UNKNOWN" : value;
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
    public record AccountRequest(@Positive long ownerId, @Positive long warehouseId, @NotBlank String sku, String batchNo, @NotNull BigDecimal qty, @NotBlank String sourceNo) {
    }
}
