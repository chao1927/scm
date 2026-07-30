package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.contract.*;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.*;
import org.springframework.security.core.annotation.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;

/**
 * SupplierContractController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/contracts")
public class SupplierContractController {

    /**
     * s（类型：{@code SupplierContractApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierContractApplicationService s;

    /**
     * c（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory c;

    /**
     * 创建 SupplierContractController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param s 业务处理参数或成员，类型为 {@code SupplierContractApplicationService}
     * @param c 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierContractController(SupplierContractApplicationService s, CommandContextFactory c) {
        this.s = s;
        this.c = c;
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
     * @param j 业务处理参数或成员，类型为 {@code Jwt}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<SupplierContractView>>}
     */
    @GetMapping
    public ApiResponse<PageResult<SupplierContractView>> page(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt j, HttpServletRequest r) {
        return ApiResponse.success(s.page(supplierId, scope(j), status, keyword, pageNo, pageSize), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param j 业务处理参数或成员，类型为 {@code Jwt}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierContractView>}
     */
    @GetMapping("/{id}")
    public ApiResponse<SupplierContractView> detail(@PathVariable long id, @AuthenticationPrincipal Jwt j, HttpServletRequest r) {
        return ApiResponse.success(s.detail(id, scope(j)), r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param b 业务处理参数或成员，类型为 {@code Create}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping
    public ApiResponse<CommandResult> create(@Valid @RequestBody Create b, HttpServletRequest r, Authentication a) {
        return ok(s.create(b.supplierId(), b.quoteId(), b.priceAgreementRef(), b.contractType(), b.effectiveFrom(), b.effectiveTo(), b.termsJson(), b.attachmentUrl(), c.create(r, a)), r);
    }

    /**
     * 处理当前类型职责中的操作 {@code modify}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Modify}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PutMapping("/{id}")
    public ApiResponse<CommandResult> modify(@PathVariable long id, @Valid @RequestBody Modify b, HttpServletRequest r, Authentication a) {
        return ok(s.modifyDraft(id, b.version(), b.effectiveTo(), b.termsJson(), b.attachmentUrl(), c.create(r, a)), r);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code V}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/submit")
    public ApiResponse<CommandResult> submit(@PathVariable long id, @Valid @RequestBody V b, HttpServletRequest r, Authentication a) {
        return ok(s.submit(id, b.version(), c.create(r, a)), r);
    }

    /**
     * 处理当前类型职责中的操作 {@code activate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code V}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/activate")
    public ApiResponse<CommandResult> activate(@PathVariable long id, @Valid @RequestBody V b, HttpServletRequest r, Authentication a) {
        return ok(s.activate(id, b.version(), c.create(r, a)), r);
    }

    /**
     * 处理当前类型职责中的操作 {@code renew}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Renew}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/renew")
    public ApiResponse<CommandResult> renew(@PathVariable long id, @Valid @RequestBody Renew b, HttpServletRequest r, Authentication a) {
        return ok(s.renew(id, b.version(), b.effectiveTo(), c.create(r, a)), r);
    }

    /**
     * 处理当前类型职责中的操作 {@code terminate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Terminate}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/terminate")
    public ApiResponse<CommandResult> terminate(@PathVariable long id, @Valid @RequestBody Terminate b, HttpServletRequest r, Authentication a) {
        return ok(s.terminate(id, b.version(), b.reason(), c.create(r, a)), r);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param x 业务处理参数或成员，类型为 {@code CommandResult}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    private ApiResponse<CommandResult> ok(CommandResult x, HttpServletRequest r) {
        return ApiResponse.success(x, r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
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
     * Create。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Create(@Positive long supplierId, Long quoteId, String priceAgreementRef, @NotBlank String contractType, @NotNull LocalDate effectiveFrom, @NotNull LocalDate effectiveTo, @NotBlank String termsJson, @NotBlank String attachmentUrl) {
    }

    /**
     * Modify。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Modify(@PositiveOrZero int version, @NotNull LocalDate effectiveTo, @NotBlank String termsJson, @NotBlank String attachmentUrl) {
    }

    /**
     * V。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record V(@PositiveOrZero int version) {
    }

    /**
     * Renew。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Renew(@PositiveOrZero int version, @NotNull LocalDate effectiveTo) {
    }

    /**
     * Terminate。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Terminate(@PositiveOrZero int version, @NotBlank String reason) {
    }
}
