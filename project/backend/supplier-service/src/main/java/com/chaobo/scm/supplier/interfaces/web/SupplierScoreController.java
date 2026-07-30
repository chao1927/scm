package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.supplier.application.score.*;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/**
 * SupplierScoreController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1")
public class SupplierScoreController {

    /**
     * service（类型：{@code SupplierScoreApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierScoreApplicationService service;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierScoreController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierScoreApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierScoreController(SupplierScoreApplicationService service, CommandContextFactory contexts) {
        this.service = service;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code rules}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<ScoreViews.Rule>>}
     */
    @GetMapping("/score-rules")
    public ApiResponse<List<ScoreViews.Rule>> rules(HttpServletRequest r) {
        return ok(service.rules(), r);
    }

    /**
     * 执行命令 {@code createRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param b 业务处理参数或成员，类型为 {@code Rule}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Long>}
     */
    @PostMapping("/score-rules")
    public ApiResponse<Long> createRule(@Valid @RequestBody Rule b, HttpServletRequest r, Authentication a) {
        return ok(service.createRule(b.name(), b.dimensionCode(), b.metricCode(), b.weight(), b.targetValue(), b.direction(), b.effectiveFrom(), b.effectiveTo(), contexts.create(r, a)), r);
    }

    /**
     * 执行命令 {@code publishRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Version}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/score-rules/{id}/publish")
    public ApiResponse<Void> publishRule(@PathVariable long id, @Valid @RequestBody Version b, HttpServletRequest r, Authentication a) {
        service.publishRule(id, b.version(), contexts.create(r, a));
        return ok(null, r);
    }

    /**
     * 执行命令 {@code disableRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Version}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/score-rules/{id}/disable")
    public ApiResponse<Void> disableRule(@PathVariable long id, @Valid @RequestBody Version b, HttpServletRequest r, Authentication a) {
        service.disableRule(id, b.version(), contexts.create(r, a));
        return ok(null, r);
    }

    /**
     * 处理当前类型职责中的操作 {@code calculate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param b 业务处理参数或成员，类型为 {@code Calculate}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<ScoreViews.Result>}
     */
    @PostMapping("/scores/calculate")
    public ApiResponse<ScoreViews.Result> calculate(@Valid @RequestBody Calculate b, HttpServletRequest r, Authentication a) {
        return ok(service.calculate(b.supplierId(), YearMonth.parse(b.periodCode()), b.manualAdjustment(), b.adjustmentReason(), contexts.create(r, a)), r);
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Version}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<ScoreViews.Result>}
     */
    @PostMapping("/scores/{id}/publish")
    public ApiResponse<ScoreViews.Result> publish(@PathVariable long id, @Valid @RequestBody Version b, HttpServletRequest r, Authentication a) {
        return ok(service.publish(id, b.version(), contexts.create(r, a)), r);
    }

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param period 业务处理参数或成员，类型为 {@code String}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<ScoreViews.Result>}
     */
    @GetMapping("/suppliers/{supplierId}/scores/{period}")
    public ApiResponse<ScoreViews.Result> result(@PathVariable long supplierId, @PathVariable String period, @AuthenticationPrincipal Jwt jwt, HttpServletRequest r) {
        return ok(service.result(supplierId, period, scope(jwt)), r);
    }

    /**
     * 处理当前类型职责中的操作 {@code risks}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<ScoreViews.Risk>>}
     */
    @GetMapping("/suppliers/{supplierId}/risks")
    public ApiResponse<List<ScoreViews.Risk>> risks(@PathVariable long supplierId, @AuthenticationPrincipal Jwt jwt, HttpServletRequest r) {
        return ok(service.risks(supplierId, scope(jwt)), r);
    }

    /**
     * 处理当前类型职责中的操作 {@code process}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code Process}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/risks/{id}/process")
    public ApiResponse<Void> process(@PathVariable long id, @Valid @RequestBody Process b, HttpServletRequest r, Authentication a) {
        service.processRisk(id, b.version(), b.accepted(), contexts.create(r, a));
        return ok(null, r);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code T}
     * @param r 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    private <T> ApiResponse<T> ok(T v, HttpServletRequest r) {
        return ApiResponse.success(v, r.getHeader("X-Request-Id"), r.getHeader("X-Trace-Id"));
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
     * Rule。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Rule(@NotBlank String name, @NotBlank String dimensionCode, @NotBlank String metricCode, @DecimalMin(value = "0", inclusive = false) @DecimalMax("1") BigDecimal weight, @DecimalMin(value = "0", inclusive = false) BigDecimal targetValue, @Min(1) @Max(2) int direction, @NotNull LocalDate effectiveFrom, LocalDate effectiveTo) {
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
     * Calculate。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Calculate(@Positive long supplierId, @Pattern(regexp = "\\d{4}-\\d{2}") String periodCode, BigDecimal manualAdjustment, String adjustmentReason) {
    }

    /**
     * Process。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Process(@PositiveOrZero int version, boolean accepted) {
    }
}
