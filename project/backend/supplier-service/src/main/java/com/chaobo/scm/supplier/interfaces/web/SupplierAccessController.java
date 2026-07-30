package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.supplier.application.account.*;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * SupplierAccessController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/supplier/v1/access")
public class SupplierAccessController {

    /**
     * service（类型：{@code SupplierAccessApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierAccessApplicationService service;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 SupplierAccessController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code SupplierAccessApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public SupplierAccessController(SupplierAccessApplicationService service, CommandContextFactory contexts) {
        this.service = service;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code contacts}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<SupplierContactView>>}
     */
    @GetMapping("/suppliers/{supplierId}/contacts")
    public ApiResponse<List<SupplierContactView>> contacts(@PathVariable long supplierId, HttpServletRequest request, Authentication authentication) {
        return ok(service.contacts(supplierId, contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code contact}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<SupplierContactView>}
     */
    @GetMapping("/contacts/{id}")
    public ApiResponse<SupplierContactView> contact(@PathVariable long id, HttpServletRequest request, Authentication authentication) {
        return ok(service.contact(id, contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code bindings}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<List<SupplierUserBindingView>>}
     */
    @GetMapping("/suppliers/{supplierId}/users")
    public ApiResponse<List<SupplierUserBindingView>> bindings(@PathVariable long supplierId, HttpServletRequest request, Authentication authentication) {
        return ok(service.bindings(supplierId, contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Contact}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/contacts")
    public ApiResponse<CommandResult> save(@Valid @RequestBody Contact body, HttpServletRequest request, Authentication authentication) {
        return ok(service.saveContact(body.contactId(), body.supplierId(), body.contactName(), body.mobile(), body.email(), body.contactRole(), body.primary(), body.version(), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code bind}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code Bind}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/users")
    public ApiResponse<CommandResult> bind(@Valid @RequestBody Bind body, HttpServletRequest request, Authentication authentication) {
        return ok(service.bind(body.supplierId(), body.userId(), body.bindingRole(), body.primary(), contexts.create(request, authentication)), request);
    }

    /**
     * 执行命令 {@code unbind}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code Version}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<CommandResult>}
     */
    @PostMapping("/users/{id}/unbind")
    public ApiResponse<CommandResult> unbind(@PathVariable long id, @Valid @RequestBody Version body, HttpServletRequest request, Authentication authentication) {
        return ok(service.unbind(id, body.version(), contexts.create(request, authentication)), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code ok}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code T}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    private <T> ApiResponse<T> ok(T value, HttpServletRequest request) {
        return ApiResponse.success(value, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    /**
     * Contact。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Contact(Long contactId, @Positive long supplierId, @NotBlank String contactName, @NotBlank String mobile, String email, @NotBlank String contactRole, boolean primary, @PositiveOrZero int version) {
    }

    /**
     * Bind。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Bind(@Positive long supplierId, @Positive long userId, @NotBlank String bindingRole, boolean primary) {
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
}
