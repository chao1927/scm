package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.comparison.BidComparisonApplicationService;
import com.chaobo.scm.purchase.application.comparison.BidComparisonCommands;
import com.chaobo.scm.purchase.application.comparison.BidComparisonQueryApplicationService;
import com.chaobo.scm.purchase.application.comparison.BidComparisonView;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
import java.time.LocalDate;
import java.util.List;

/**
 * BidComparisonController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/purchase/v1/bid-comparisons")
public class BidComparisonController {

    /**
     * applicationService（类型：{@code BidComparisonApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final BidComparisonApplicationService applicationService;

    /**
     * queryService（类型：{@code BidComparisonQueryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final BidComparisonQueryApplicationService queryService;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 BidComparisonController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param applicationService 应用或外部协作依赖，类型为 {@code BidComparisonApplicationService}
     * @param queryService 应用或外部协作依赖，类型为 {@code BidComparisonQueryApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public BidComparisonController(BidComparisonApplicationService applicationService, BidComparisonQueryApplicationService queryService, CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<BidComparisonView>>}
     */
    @GetMapping
    public ApiResponse<PageResult<BidComparisonView>> page(@RequestParam(required = false) Long purchaseOrgId, @RequestParam(required = false) Integer status, @RequestParam(required = false) String rfqNo, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, HttpServletRequest request) {
        return ok(queryService.page(purchaseOrgId, optionalLong(request.getHeader("X-Purchase-Org-Id")), status, rfqNo, pageNo, pageSize), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param compareNo 可追踪业务编码，类型为 {@code String}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<BidComparisonView>}
     */
    @GetMapping("/{compareNo}")
    public ApiResponse<BidComparisonView> detail(@PathVariable String compareNo, HttpServletRequest request) {
        return ok(queryService.detail(compareNo, optionalLong(request.getHeader("X-Purchase-Org-Id"))), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code generate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code GenerateRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping
    public ApiResponse<CommandResult> generate(@Valid @RequestBody GenerateRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.generate(body.toCommand(), contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code award}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param compareNo 可追踪业务编码，类型为 {@code String}
     * @param body 业务处理参数或成员，类型为 {@code AwardRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{compareNo}/award")
    public ApiResponse<CommandResult> award(@PathVariable String compareNo, @Valid @RequestBody AwardRequest body, HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.award(compareNo, body.toCommand(), contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param data 业务处理参数或成员，类型为 {@code T}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code optionalLong}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private static Long optionalLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    /**
     * GenerateRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record GenerateRequest(@NotBlank String rfqNo, @Positive long purchaseOrgId, @NotBlank String currency, @NotEmpty List<@Valid CandidateRequest> candidates) {

        /**
         * 转换数据模型 {@code toCommand}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code BidComparisonCommands.Generate}
         */
        BidComparisonCommands.Generate toCommand() {
            return new BidComparisonCommands.Generate(rfqNo, purchaseOrgId, currency, candidates.stream().map(CandidateRequest::toCommand).toList());
        }
    }

    /**
     * CandidateRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CandidateRequest(Long candidateId, @Positive long supplierId, String supplierName, @NotBlank String quoteNo, @NotBlank String skuCode, @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quoteQty, @NotNull @DecimalMin("0") BigDecimal unitPrice, @NotNull @DecimalMin("0") BigDecimal taxRate, @PositiveOrZero int deliveryDays, @DecimalMin("0") BigDecimal supplierScore, @DecimalMin("0") BigDecimal transportScore, @DecimalMin("0") BigDecimal estimatedFreightCost) {

        /**
         * 转换数据模型 {@code toCommand}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code BidComparisonCommands.Candidate}
         */
        BidComparisonCommands.Candidate toCommand() {
            return new BidComparisonCommands.Candidate(candidateId, supplierId, supplierName, quoteNo, skuCode, quoteQty, unitPrice, taxRate, deliveryDays, supplierScore, transportScore, estimatedFreightCost);
        }
    }

    /**
     * AwardRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record AwardRequest(@PositiveOrZero int version, @Positive long candidateId, @NotBlank String reason, boolean activatePurchasePrice, @Min(1) @Max(3) int priceType, LocalDate effectiveFrom, LocalDate effectiveTo) {

        /**
         * 转换数据模型 {@code toCommand}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 转换数据模型的结果，类型为 {@code BidComparisonCommands.Award}
         */
        BidComparisonCommands.Award toCommand() {
            return new BidComparisonCommands.Award(version, candidateId, reason, activatePurchasePrice, priceType, effectiveFrom, effectiveTo);
        }
    }
}
