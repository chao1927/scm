package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.finance.*;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/**
 * SupplierFinanceController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1")
public class SupplierFinanceController {

    /**
     * service（类型：{@code SupplierFinanceApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierFinanceApplicationService service;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierFinanceController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierFinanceApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierFinanceController(SupplierFinanceApplicationService service, CommandContextFactory contexts) {
        this.service = service;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code reconciliations}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<FinanceViews.Reconciliation>>}
     */
    @GetMapping("/reconciliations")
    public ApiResponse<PageResult<FinanceViews.Reconciliation>> reconciliations(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest r) {
        return ApiResponse.success(service.reconciliations(supplierId, scope(jwt), status, pageNo, pageSize), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Confirm}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/reconciliations/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable long id, @Valid @RequestBody Confirm b, HttpServletRequest r, Authentication a) {
        service.confirm(id, b.version(), b.confirmedAmount(), b.differenceReason(), contexts.create(r, a));
        return ok(r);
    }

    /**
     * 处理当前类型职责中的操作 {@code withdraw}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Version}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/reconciliations/{id}/withdraw")
    public ApiResponse<Void> withdraw(@PathVariable long id, @Valid @RequestBody Version b, HttpServletRequest r, Authentication a) {
        service.withdraw(id, b.version(), contexts.create(r, a));
        return ok(r);
    }

    /**
     * 处理当前类型职责中的操作 {@code invoices}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<FinanceViews.Invoice>>}
     */
    @GetMapping("/invoices")
    public ApiResponse<PageResult<FinanceViews.Invoice>> invoices(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest r) {
        return ApiResponse.success(service.invoices(supplierId, scope(jwt), status, pageNo, pageSize), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code invoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param b 业务处理参数或成员，类型为 {@code Invoice}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Long>}
     */
    @PostMapping("/invoices")
    public ApiResponse<Long> invoice(@Valid @RequestBody Invoice b, HttpServletRequest r, Authentication a) {
        return ApiResponse.success(service.uploadInvoice(b.invoiceNo(), b.supplierId(), b.reconciliationId(), b.invoiceType(), b.amountExcludingTax(), b.taxAmount(), b.taxRate(), b.attachmentUrl(), contexts.create(r, a)), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    private ApiResponse<Void> ok(HttpServletRequest r) {
        return ApiResponse.success(null, r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code scope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param j 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long scope(Jwt j) {
        Number n = j.hasClaim("supplier_id") ? j.getClaim("supplier_id") : null;
        return n == null ? null : n.longValue();
    }

    /**
     * Confirm。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Confirm(@PositiveOrZero int version, @DecimalMin("0") BigDecimal confirmedAmount, String differenceReason) {
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
     * Invoice。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Invoice(@NotBlank String invoiceNo, @Positive long supplierId, Long reconciliationId, @Min(1) @Max(3) int invoiceType, @DecimalMin("0") BigDecimal amountExcludingTax, @DecimalMin("0") BigDecimal taxAmount, @DecimalMin("0") BigDecimal taxRate, @NotBlank String attachmentUrl) {
    }
}
