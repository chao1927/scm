package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.qualification.*;
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
 * SupplierQualificationController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/qualifications")
public class SupplierQualificationController {

    /**
     * service（类型：{@code SupplierQualificationApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualificationApplicationService service;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierQualificationController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierQualificationApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierQualificationController(SupplierQualificationApplicationService service, CommandContextFactory contexts) {
        this.service = service;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<PageResult<SupplierQualificationView>>}
     */
    @GetMapping
    public ApiResponse<PageResult<SupplierQualificationView>> page(@RequestParam(required = false) Long supplierId, @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ApiResponse.success(service.page(supplierId, scope(jwt), status, pageNo, pageSize), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierQualificationView>}
     */
    @GetMapping("/{id}")
    public ApiResponse<SupplierQualificationView> detail(@PathVariable long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return ApiResponse.success(service.detail(id, scope(jwt)), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Submit}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param auth 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping
    public ApiResponse<CommandResult> submit(@Valid @RequestBody Submit body, HttpServletRequest request, Authentication auth) {
        return ok(service.submit(body.supplierId(), body.qualificationType(), body.qualificationNo(), body.validFrom(), body.validTo(), body.attachmentUrl(), contexts.create(request, auth)), request);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Approve}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param auth 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<CommandResult> approve(@PathVariable long id, @Valid @RequestBody Approve body, HttpServletRequest request, Authentication auth) {
        return ok(service.approve(id, body.version(), body.remark(), contexts.create(request, auth)), request);
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Reject}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param auth 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/{id}/reject")
    public ApiResponse<CommandResult> reject(@PathVariable long id, @Valid @RequestBody Reject body, HttpServletRequest request, Authentication auth) {
        return ok(service.reject(id, body.version(), body.reason(), contexts.create(request, auth)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param result 处理结果，类型为 {@code CommandResult}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    private ApiResponse<CommandResult> ok(CommandResult result, HttpServletRequest request) {
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * 处理当前类型职责中的操作 {@code scope}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param jwt 业务处理参数或成员，类型为 {@code Jwt}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    private Long scope(Jwt jwt) {
        Number n = jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null;
        return n == null ? null : n.longValue();
    }

    /**
     * Submit。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Submit(@Positive long supplierId, @NotBlank String qualificationType, @NotBlank String qualificationNo, @NotNull LocalDate validFrom, @NotNull LocalDate validTo, @NotBlank String attachmentUrl) {
    }

    /**
     * Approve。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Approve(@PositiveOrZero int version, String remark) {
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
