package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmOpenApiApplicationService;
import com.chaobo.scm.common.security.ScmAccessContexts;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MdmInternalEventController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/internal/mdm/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'mdm:event:consume')")
public class MdmInternalEventController {

    /**
     * service（类型：{@code MdmOpenApiApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmOpenApiApplicationService service;

    /**
     * 创建 MdmInternalEventController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code MdmOpenApiApplicationService}
     */
    public MdmInternalEventController(MdmOpenApiApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code MdmOpenApiApplicationService.EventEnvelope}
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 执行命令的结果，类型为 {@code MdmOpenApiApplicationService.ConsumeResult}
     */
    @PostMapping("/events")
    public MdmOpenApiApplicationService.ConsumeResult consume(@RequestBody MdmOpenApiApplicationService.EventEnvelope event, Authentication authentication) {
        ScmAccessContexts.require(authentication).requireApplication(event.sourceSystem());
        return service.consumeEvent(event);
    }
}
