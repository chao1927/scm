package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamAdminApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IamInternalEventController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/internal/iam/v1")
public class IamInternalEventController {

    /**
     * service（类型：{@code IamAdminApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamAdminApplicationService service;

    /**
     * 创建 IamInternalEventController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code IamAdminApplicationService}
     */
    public IamInternalEventController(IamAdminApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code IamAdminApplicationService.EventEnvelope}
     * @return 执行命令的结果，类型为 {@code IamAdminApplicationService.ConsumeResult}
     */
    @PostMapping("/events")
    public IamAdminApplicationService.ConsumeResult consume(@RequestBody IamAdminApplicationService.EventEnvelope event) {
        return service.consumeEvent(event);
    }
}
