package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.quote.*;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.domain.quote.QuoteLine;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * SupplierQuoteController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/quotes")
public class SupplierQuoteController {

    /**
     * service（类型：{@code SupplierQuoteApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQuoteApplicationService service;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierQuoteController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierQuoteApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierQuoteController(SupplierQuoteApplicationService service, CommandContextFactory contexts) {
        this.service = service;
        this.contexts = contexts;
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
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<SupplierQuoteView>>}
     */
    @GetMapping
    public ApiResponse<PageResult<SupplierQuoteView>> page(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ApiResponse.success(service.page(supplierId, scope(jwt), status, keyword, pageNo, pageSize), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierQuoteView>}
     */
    @GetMapping("/{id}")
    public ApiResponse<SupplierQuoteView> detail(@PathVariable long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ApiResponse.success(service.detail(id, scope(jwt)), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Create}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping
    public ApiResponse<CommandResult> create(@Valid @RequestBody Create body, HttpServletRequest request, Authentication authentication) {
        return ok(service.create(body.supplierId(), body.rfqId(), body.rfqNo(), body.currency(), body.validFrom(), body.validTo(), toLines(body.lines()), contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code modifyDraft}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Modify}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PutMapping("/{id}")
    public ApiResponse<CommandResult> modifyDraft(@PathVariable long id, @Valid @RequestBody Modify body, HttpServletRequest request, Authentication authentication) {
        return ok(service.modifyDraft(id, body.version(), body.validFrom(), body.validTo(), toLines(body.lines()), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Version}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/submit")
    public ApiResponse<CommandResult> submit(@PathVariable long id, @Valid @RequestBody Version body, HttpServletRequest request, Authentication authentication) {
        return ok(service.submit(id, body.version(), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Version}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/confirm")
    public ApiResponse<CommandResult> confirm(@PathVariable long id, @Valid @RequestBody Version body, HttpServletRequest request, Authentication authentication) {
        return ok(service.confirm(id, body.version(), contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code adopt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Adopt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/adopt")
    public ApiResponse<CommandResult> adopt(@PathVariable long id, @Valid @RequestBody Adopt body, HttpServletRequest request, Authentication authentication) {
        return ok(service.adopt(id, body.version(), body.priceAgreementRef(), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Reject}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/reject")
    public ApiResponse<CommandResult> reject(@PathVariable long id, @Valid @RequestBody Reject body, HttpServletRequest request, Authentication authentication) {
        return ok(service.reject(id, body.version(), body.reason(), contexts.create(request, authentication)), request);
    }

    /**
     * 转换数据模型 {@code toLines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<Line>}
     * @return 转换数据模型的结果，类型为 {@code List<QuoteLine>}
     */
    private List<QuoteLine> toLines(List<Line> lines) {
        return lines.stream().map(line -> new QuoteLine(line.lineId() == null ? 0 : line.lineId(), line.skuCode(), line.quoteQty(), line.unitPrice(), line.taxRate(), line.deliveryDays(), line.moq())).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code CommandResult}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    private ApiResponse<CommandResult> ok(CommandResult value, HttpServletRequest request) {
        return ApiResponse.success(value, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code scope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long scope(Jwt jwt) {
        Number value = jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null;
        return value == null ? null : value.longValue();
    }

    /**
     * Create。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Create(@Positive long supplierId, Long rfqId, String rfqNo, @NotBlank String currency, @NotNull LocalDate validFrom, @NotNull LocalDate validTo, @NotEmpty List<Line> lines) {
    }

    /**
     * Modify。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Modify(@PositiveOrZero int version, @NotNull LocalDate validFrom, @NotNull LocalDate validTo, @NotEmpty List<Line> lines) {
    }

    /**
     * Line。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Line(Long lineId, @NotBlank String skuCode, @NotNull @DecimalMin("0.0001") BigDecimal quoteQty, @NotNull @DecimalMin("0.000001") BigDecimal unitPrice, @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal taxRate, @PositiveOrZero int deliveryDays, @NotNull @DecimalMin("0.0001") BigDecimal moq) {
    }

    /**
     * Version。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Version(@PositiveOrZero int version) {
    }

    /**
     * Adopt。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Adopt(@PositiveOrZero int version, @NotBlank String priceAgreementRef) {
    }

    /**
     * Reject。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Reject(@PositiveOrZero int version, @NotBlank String reason) {
    }
}
