package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.supplier.application.asn.AsnCommandApplicationService;
import com.chaobo.scm.supplier.application.asn.AsnCommands;
import com.chaobo.scm.supplier.application.asn.AsnDetailView;
import com.chaobo.scm.supplier.application.asn.AsnSummaryView;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.supplier.application.asn.AsnQueryApplicationService;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.domain.asn.AsnAggregate;
import com.chaobo.scm.supplier.domain.asn.ShipmentInfo;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * AsnController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/asns")
public class AsnController {

    /**
     * commandService（类型：{@code AsnCommandApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final AsnCommandApplicationService commandService;

    /**
     * queryService（类型：{@code AsnQueryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final AsnQueryApplicationService queryService;

    /**
     * contextFactory（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contextFactory;

    /**
     * 创建 AsnController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param commandService 应用或外部协作依赖，类型为 {@code AsnCommandApplicationService}
     * @param queryService 应用或外部协作依赖，类型为 {@code AsnQueryApplicationService}
     * @param contextFactory 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public AsnController(AsnCommandApplicationService commandService, AsnQueryApplicationService queryService, CommandContextFactory contextFactory) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.contextFactory = contextFactory;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code CreateAsnRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommandResult> create(@Valid @RequestBody CreateAsnRequest body, HttpServletRequest request, Authentication authentication) {
        var lines = body.lines().stream().map(line -> new AsnAggregate.NewLine(line.skuCode(), line.plannedQuantity(), line.batchNo(), line.productionDate(), line.expireDate())).toList();
        var command = new AsnCommands.Create(body.purchaseOrderId(), body.supplierId(), body.warehouseId(), body.estimatedArrivalAt(), lines);
        CommandResult result = commandService.create(command, contextFactory.create(request, authentication));
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{asnId}/submit")
    public ApiResponse<CommandResult> submit(@PathVariable long asnId, @Valid @RequestBody VersionRequest body, HttpServletRequest request, Authentication authentication) {
        CommandResult result = commandService.submit(new AsnCommands.Submit(asnId, body.version()), contextFactory.create(request, authentication));
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code CancelAsnRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{asnId}/cancel")
    public ApiResponse<CommandResult> cancel(@PathVariable long asnId, @Valid @RequestBody CancelAsnRequest body, HttpServletRequest request, Authentication authentication) {
        CommandResult result = commandService.cancel(new AsnCommands.Cancel(asnId, body.reason(), body.version()), contextFactory.create(request, authentication));
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code ship}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code ShipAsnRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{asnId}/ship")
    public ApiResponse<CommandResult> ship(@PathVariable long asnId, @Valid @RequestBody ShipAsnRequest body, HttpServletRequest request, Authentication authentication) {
        var command = new AsnCommands.ConfirmShipment(asnId, new ShipmentInfo(body.shippedAt(), body.carrierName(), body.trackingNo()), body.version());
        CommandResult result = commandService.confirmShipment(command, contextFactory.create(request, authentication));
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<AsnDetailView>}
     */
    @GetMapping("/{asnId}")
    @PreAuthorize("hasAuthority('supplier:asn:read')")
    public ApiResponse<AsnDetailView> detail(@PathVariable long asnId, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        Long supplierScopeId = supplierScope(jwt);
        return ApiResponse.success(queryService.detail(asnId, supplierScopeId), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<AsnSummaryView>>}
     */
    @GetMapping
    @PreAuthorize("hasAuthority('supplier:asn:read')")
    public ApiResponse<PageResult<AsnSummaryView>> page(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        Long supplierScopeId = supplierScope(jwt);
        return ApiResponse.success(queryService.page(supplierId, supplierScopeId, status, keyword, pageNo, pageSize), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * CreateAsnRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateAsnRequest(@Positive long purchaseOrderId, @Positive long supplierId, @Positive long warehouseId, @NotNull @Future OffsetDateTime estimatedArrivalAt, @NotEmpty List<@Valid CreateAsnLineRequest> lines) {
    }

    /**
     * CreateAsnLineRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateAsnLineRequest(@NotBlank String skuCode, @NotNull @Positive BigDecimal plannedQuantity, String batchNo, LocalDate productionDate, LocalDate expireDate) {
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

    /**
     * CancelAsnRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CancelAsnRequest(@NotBlank String reason, @PositiveOrZero int version) {
    }

    /**
     * ShipAsnRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ShipAsnRequest(@NotNull OffsetDateTime shippedAt, @NotBlank String carrierName, String trackingNo, @PositiveOrZero int version) {
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierScope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long supplierScope(Jwt jwt) {
        Number claim = jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null;
        return claim == null ? null : claim.longValue();
    }
}
