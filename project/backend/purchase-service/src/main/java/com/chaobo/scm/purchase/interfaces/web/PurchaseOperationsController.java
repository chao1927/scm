package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.purchase.application.integration.InboundEventReplayApplicationService;
import com.chaobo.scm.purchase.application.operations.PurchaseOperationsApplicationService;
import com.chaobo.scm.purchase.application.operations.PurchaseOperationsViews;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * PurchaseOperationsController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/purchase/v1/operations")
public class PurchaseOperationsController {

    /**
     * operations（类型：{@code PurchaseOperationsApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOperationsApplicationService operations;

    /**
     * replayService（类型：{@code InboundEventReplayApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventReplayApplicationService replayService;

    /**
     * contexts（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory contexts;

    /**
     * 创建 PurchaseOperationsController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param operations 业务处理参数或成员，类型为 {@code PurchaseOperationsApplicationService}
     * @param replayService 应用或外部协作依赖，类型为 {@code InboundEventReplayApplicationService}
     * @param contexts 业务处理参数或成员，类型为 {@code CommandContextFactory}
     */
    public PurchaseOperationsController(PurchaseOperationsApplicationService operations, InboundEventReplayApplicationService replayService, CommandContextFactory contexts) {
        this.operations = operations;
        this.replayService = replayService;
        this.contexts = contexts;
    }

    /**
     * 处理当前类型职责中的操作 {@code failedEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<PurchaseOperationsViews.FailedEvent>>}
     */
    @GetMapping("/failed-events")
    public ApiResponse<List<PurchaseOperationsViews.FailedEvent>> failedEvents(HttpServletRequest request) {
        return ok(operations.failedEvents(), request);
    }

    /**
     * 处理当前类型职责中的操作 {@code failedCommands}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<PurchaseOperationsViews.FailedCommand>>}
     */
    @GetMapping("/failed-commands")
    public ApiResponse<List<PurchaseOperationsViews.FailedCommand>> failedCommands(HttpServletRequest request) {
        return ok(operations.failedCommands(), request);
    }

    /**
     * 执行命令 {@code replayCommand}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code ReplayRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/failed-commands/{id}/replay")
    public ApiResponse<Void> replayCommand(@PathVariable long id, @Valid @RequestBody ReplayRequest body, HttpServletRequest request, Authentication authentication) {
        operations.replayCommand(id, contexts.create(request, authentication));
        return ok(null, request);
    }

    /**
     * 执行命令 {@code replay}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param body 业务处理参数或成员，类型为 {@code ReplayRequest}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<Void>}
     */
    @PostMapping("/failed-events/{id}/replay")
    public ApiResponse<Void> replay(@PathVariable long id, @Valid @RequestBody ReplayRequest body, HttpServletRequest request, Authentication authentication) {
        replayService.replay(id, body.reason(), contexts.create(request, authentication));
        return ok(null, request);
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
     * ReplayRequest。
     *
     * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReplayRequest(@NotBlank String reason) {
    }
}
