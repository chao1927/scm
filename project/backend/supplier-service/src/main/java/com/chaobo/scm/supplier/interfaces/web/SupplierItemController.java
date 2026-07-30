package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.item.*;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.domain.item.SupplyCondition;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SupplierItemController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/items")
public class SupplierItemController {

    /**
     * service（类型：{@code SupplierItemApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemApplicationService service;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierItemController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierItemApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierItemController(SupplierItemApplicationService service, CommandContextFactory contexts) {
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
     * @param req 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<SupplierItemView>>}
     */
    @GetMapping
    @PreAuthorize("hasAuthority('supplier:sku:read')")
    public ApiResponse<PageResult<SupplierItemView>> page(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest req) {
        return ApiResponse.success(service.page(supplierId, scope(jwt), status, keyword, pageNo, pageSize), req.getHeader("X-Request-Id"), req.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param req 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierItemView>}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:sku:read')")
    public ApiResponse<SupplierItemView> detail(@PathVariable long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest req) {
        return ApiResponse.success(service.detail(id, scope(jwt)), req.getHeader("X-Request-Id"), req.getHeader("X-Trace-Id"));
    }

    /**
     * 执行命令 {@code enable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param b 业务处理参数或成员，类型为 {@code EnableRequest}
     * @param req 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping
    public ApiResponse<CommandResult> enable(@Valid @RequestBody EnableRequest b, HttpServletRequest req, Authentication a) {
        return ok(service.enable(b.supplierId(), b.skuCode(), b.supplierSkuCode(), b.condition(), contexts.create(req, a)), req);
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code ChangeRequest}
     * @param req 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PutMapping("/{id}")
    public ApiResponse<CommandResult> change(@PathVariable long id, @Valid @RequestBody ChangeRequest b, HttpServletRequest req, Authentication a) {
        return ok(service.change(id, b.version(), b.supplierSkuCode(), b.condition(), contexts.create(req, a)), req);
    }

    /**
     * 处理当前类型职责中的操作 {@code pause}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code StateRequest}
     * @param req 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/pause")
    public ApiResponse<CommandResult> pause(@PathVariable long id, @Valid @RequestBody StateRequest b, HttpServletRequest req, Authentication a) {
        return ok(service.pause(id, b.version(), b.reason(), contexts.create(req, a)), req);
    }

    /**
     * 处理当前类型职责中的操作 {@code resume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code VersionRequest}
     * @param req 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/resume")
    public ApiResponse<CommandResult> resume(@PathVariable long id, @Valid @RequestBody VersionRequest b, HttpServletRequest req, Authentication a) {
        return ok(service.resume(id, b.version(), contexts.create(req, a)), req);
    }

    /**
     * 处理当前类型职责中的操作 {@code discontinue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param b 业务处理参数或成员，类型为 {@code StateRequest}
     * @param req 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @param a 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/discontinue")
    public ApiResponse<CommandResult> discontinue(@PathVariable long id, @Valid @RequestBody StateRequest b, HttpServletRequest req, Authentication a) {
        return ok(service.discontinue(id, b.version(), b.reason(), contexts.create(req, a)), req);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code CommandResult}
     * @param q 业务处理参数或成员，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    private ApiResponse<CommandResult> ok(CommandResult r, HttpServletRequest q) {
        return ApiResponse.success(r, q.getHeader("X-Request-Id"), q.getHeader("X-Trace-Id"));
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
     * EnableRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EnableRequest(@Positive long supplierId, @NotBlank String skuCode, String supplierSkuCode, @NotNull BigDecimal moq, @NotNull BigDecimal mpq, @PositiveOrZero int leadTimeDays, @NotBlank String purchaseUnit, LocalDate effectiveFrom, LocalDate effectiveTo) {

        /**
         * 处理当前类型职责中的操作 {@code condition}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplyCondition}
         */
        SupplyCondition condition() {
            return new SupplyCondition(moq, mpq, leadTimeDays, purchaseUnit, effectiveFrom, effectiveTo);
        }
    }

    /**
     * ChangeRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ChangeRequest(String supplierSkuCode, @NotNull BigDecimal moq, @NotNull BigDecimal mpq, @PositiveOrZero int leadTimeDays, @NotBlank String purchaseUnit, LocalDate effectiveFrom, LocalDate effectiveTo, @PositiveOrZero int version) {

        /**
         * 处理当前类型职责中的操作 {@code condition}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplyCondition}
         */
        SupplyCondition condition() {
            return new SupplyCondition(moq, mpq, leadTimeDays, purchaseUnit, effectiveFrom, effectiveTo);
        }
    }

    /**
     * StateRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record StateRequest(@PositiveOrZero int version, @NotBlank String reason) {
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
