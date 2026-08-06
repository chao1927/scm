package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.inbox.WmsInboundEventApplicationService;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import java.util.List;

/**
 * WmsInboundEventController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:event:manage')")
public class WmsInboundEventController {

    /**
     * service（类型：{@code WmsInboundEventApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final WmsInboundEventApplicationService service;

    /**
     * 创建 WmsInboundEventController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code WmsInboundEventApplicationService}
     */
    public WmsInboundEventController(WmsInboundEventApplicationService service) {
        this.service = service;
    }

    /**
     * 处理当前类型职责中的操作 {@code failed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<List<WmsInboundEventApplicationService.FailedEventView>>}
     */
    @GetMapping("/api/wms/v1/operations/inbox/failed-events")
    public ApiResponse<List<WmsInboundEventApplicationService.FailedEventView>> failed(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.failedEvents(limit), request);
    }

    /**
     * 执行命令 {@code replay}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param inboxId 业务或技术标识，类型为 {@code long}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code ApiResponse<WmsInboundEventApplicationService.ConsumeResult>}
     */
    @PostMapping("/api/wms/v1/operations/inbox/failed-events/{inboxId}/replay")
    public ApiResponse<WmsInboundEventApplicationService.ConsumeResult> replay(@PathVariable long inboxId, HttpServletRequest request, Authentication authentication) {
        return ok(service.replay(inboxId, WmsAccessControl.operatorId(authentication)), request);
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

}
