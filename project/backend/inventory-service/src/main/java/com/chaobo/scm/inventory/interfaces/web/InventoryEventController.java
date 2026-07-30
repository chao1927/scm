package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.inventory.application.InventoryEventApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import com.chaobo.scm.common.security.ScmAccessContexts;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * InventoryEventController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'inventory:*', 'inventory:event:manage')")
public class InventoryEventController {

    /**
     * service（类型：{@code InventoryEventApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InventoryEventApplicationService service;

    /**
     * 创建 InventoryEventController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code InventoryEventApplicationService}
     */
    public InventoryEventController(InventoryEventApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code EventRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<InventoryEventApplicationService.ConsumeResult>}
     */
    @PostMapping("/internal/inventory/v1/events")
    public ApiResponse<InventoryEventApplicationService.ConsumeResult> consume(@Valid @RequestBody EventRequest body, HttpServletRequest request, Authentication authentication) {
        ScmAccessContexts.require(authentication).requireApplication(body.sourceSystem());
        return ok(service.consumeWmsEvent(new InventoryEventApplicationService.EventEnvelope(body.sourceSystem(), body.eventCode(), body.eventType(), body.payload())), request);
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param body 业务处理参数或成员，类型为 {@code DispatchRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 执行命令的结果，类型为 {@code ApiResponse<InventoryEventApplicationService.DispatchResult>}
     */
    @PostMapping("/api/inventory/v1/operations/outbox/dispatch")
    public ApiResponse<InventoryEventApplicationService.DispatchResult> dispatch(@Valid @RequestBody DispatchRequest body, HttpServletRequest request) {
        return ok(service.dispatch(body.limit()), request);
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
     * EventRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EventRequest(@NotBlank String sourceSystem, @NotBlank String eventCode, @NotBlank String eventType, @NotBlank String payload) {
    }

    /**
     * DispatchRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record DispatchRequest(@Positive int limit) {
    }
}
