package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.inventory.application.StockTransferApplicationService;
import com.chaobo.scm.inventory.infrastructure.security.InventoryAccessControl;
import com.chaobo.scm.common.security.ScmAccessContexts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import java.util.List;

/**
 * StockTransferController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/inventory/v1/transfers")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'inventory:*', 'inventory:transfer:manage')")
public class StockTransferController {

    /**
     * service（类型：{@code StockTransferApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final StockTransferApplicationService service;

    /**
     * 创建 StockTransferController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code StockTransferApplicationService}
     */
    public StockTransferController(StockTransferApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreateRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<StockTransferApplicationService.TransferResult>}
     */
    @PostMapping
    public ApiResponse<StockTransferApplicationService.TransferResult> create(@Valid @RequestBody CreateRequest body, HttpServletRequest request, Authentication authentication) {
        requireScope(authentication, body.ownerId(), body.sourceWarehouseId(), body.targetWarehouseId());
        return ok(service.create(new StockTransferApplicationService.CreateCommand(body.ownerId(), body.sourceWarehouseId(), body.targetWarehouseId(), body.sku(), body.batchNo(), body.qty()), request.getHeader("X-Idempotency-Key")), request);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<StockTransferApplicationService.TransferResult>}
     */
    @PostMapping("/{transferNo}/submit")
    public ApiResponse<StockTransferApplicationService.TransferResult> submit(@PathVariable String transferNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request, Authentication authentication) {
        requireScope(authentication, service.detail(transferNo));
        return ok(service.submit(transferNo, body.version()), request);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<StockTransferApplicationService.TransferResult>}
     */
    @PostMapping("/{transferNo}/approve")
    public ApiResponse<StockTransferApplicationService.TransferResult> approve(@PathVariable String transferNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request, Authentication authentication) {
        requireScope(authentication, service.detail(transferNo));
        return ok(service.approve(transferNo, body.version()), request);
    }

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<StockTransferApplicationService.TransferResult>}
     */
    @PostMapping("/{transferNo}/reserve")
    public ApiResponse<StockTransferApplicationService.TransferResult> reserve(@PathVariable String transferNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request, Authentication authentication) {
        requireScope(authentication, service.detail(transferNo));
        return ok(service.reserve(transferNo, body.version()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code outbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code QuantityRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<StockTransferApplicationService.TransferResult>}
     */
    @PostMapping("/{transferNo}/outbound")
    public ApiResponse<StockTransferApplicationService.TransferResult> outbound(@PathVariable String transferNo, @Valid @RequestBody QuantityRequest body, HttpServletRequest request, Authentication authentication) {
        requireScope(authentication, service.detail(transferNo));
        return ok(service.recordOutbound(transferNo, body.qty(), body.version()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code inTransit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<StockTransferApplicationService.TransferResult>}
     */
    @PostMapping("/{transferNo}/in-transit")
    public ApiResponse<StockTransferApplicationService.TransferResult> inTransit(@PathVariable String transferNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request, Authentication authentication) {
        requireScope(authentication, service.detail(transferNo));
        return ok(service.markInTransit(transferNo, body.version()), request);
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<StockTransferApplicationService.TransferResult>}
     */
    @PostMapping("/{transferNo}/cancel")
    public ApiResponse<StockTransferApplicationService.TransferResult> cancel(@PathVariable String transferNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request, Authentication authentication) {
        requireScope(authentication, service.detail(transferNo));
        return ok(service.cancel(transferNo, body.version()), request);
    }

    /**
     * 执行命令 {@code confirmDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<StockTransferApplicationService.TransferResult>}
     */
    @PostMapping("/{transferNo}/difference/confirm")
    public ApiResponse<StockTransferApplicationService.TransferResult> confirmDifference(@PathVariable String transferNo, @Valid @RequestBody DifferenceConfirmationRequest body, HttpServletRequest request, Authentication authentication) {
        requireScope(authentication, service.detail(transferNo));
        return ok(service.confirmDifference(transferNo, body.reason(), body.responsibleParty(), body.evidenceRef(), body.version()), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<StockTransferApplicationService.TransferResult>}
     */
    @GetMapping("/{transferNo}")
    public ApiResponse<StockTransferApplicationService.TransferResult> detail(@PathVariable String transferNo, HttpServletRequest request, Authentication authentication) {
        var result = service.detail(transferNo);
        requireScope(authentication, result);
        return ok(result, request);
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 查询并返回的结果，类型为 {@code ApiResponse<List<StockTransferApplicationService.TransferResult>>}
     */
    @GetMapping
    public ApiResponse<List<StockTransferApplicationService.TransferResult>> list(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request, Authentication authentication) {
        var access = ScmAccessContexts.require(authentication);
        var visible = service.list(limit).stream().filter(item -> access.allowsScope("OWNER", String.valueOf(item.ownerId()))).filter(item -> access.allowsScope("WAREHOUSE", String.valueOf(item.sourceWarehouseId()))).filter(item -> access.allowsScope("WAREHOUSE", String.valueOf(item.targetWarehouseId()))).toList();
        return ok(visible, request);
    }

    /**
     * 查询并返回 {@code requireScope}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @param transfer 业务处理参数或成员，类型为 {@code StockTransferApplicationService.TransferResult}
     */
    private static void requireScope(Authentication authentication, StockTransferApplicationService.TransferResult transfer) {
        requireScope(authentication, transfer.ownerId(), transfer.sourceWarehouseId(), transfer.targetWarehouseId());
    }

    /**
     * 查询并返回 {@code requireScope}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param sourceWarehouseId 业务或技术标识，类型为 {@code long}
     * @param targetWarehouseId 业务或技术标识，类型为 {@code long}
     */
    private static void requireScope(Authentication authentication, long ownerId, long sourceWarehouseId, long targetWarehouseId) {
        InventoryAccessControl.requireAccountScope(authentication, ownerId, sourceWarehouseId);
        InventoryAccessControl.requireAccountScope(authentication, ownerId, targetWarehouseId);
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
    public record CreateRequest(@Positive long ownerId, @Positive long sourceWarehouseId, @Positive long targetWarehouseId, @NotBlank String sku, String batchNo, @NotNull @Positive BigDecimal qty) {
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

    /** 调拨差异确认命令。 */
    public record DifferenceConfirmationRequest(@NotBlank String reason, @NotBlank String responsibleParty, @NotBlank String evidenceRef, @PositiveOrZero int version) {
    }

    /**
     * QuantityRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record QuantityRequest(@NotNull @Positive BigDecimal qty, @PositiveOrZero int version) {
    }
}
